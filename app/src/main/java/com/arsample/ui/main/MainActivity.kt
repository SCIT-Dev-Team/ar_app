package com.arsample.ui.main

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.arsample.R
import com.arsample.data.models.Artifact
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.rendering.FixedWidthViewSizer
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), Scene.OnUpdateListener {
    private lateinit var arSceneFragment: ARSceneFragment
    private val mainViewModel: MainViewModel by viewModels()
    private val artifacts: ArrayList<Artifact> = ArrayList()

    /**
     * Track loading models so as to load models only once
     */
    private val loadingModels: ArrayList<String> = ArrayList()
    private val modelRenderables: HashMap<String, ModelRenderable> = HashMap()

    /**
     * Track loading detail views
     */
    private val loadingViews: ArrayList<String> = ArrayList()
    private val viewRenderables: HashMap<String, ViewRenderable> = HashMap()

    private val loadingIndicatorRenderables: HashMap<String, ViewRenderable> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        arSceneFragment =
            supportFragmentManager.findFragmentById(R.id.ar_fragment) as ARSceneFragment
        arSceneFragment.arSceneView.scene.addOnUpdateListener(this)
    }

    fun setupDatabase(session: Session, config: Config) {
        lifecycleScope.launch {
            mainViewModel.getArtifacts().collectLatest { artifacts ->
                this@MainActivity.artifacts.clear()
                this@MainActivity.artifacts.addAll(artifacts)
                val imageDatabase = AugmentedImageDatabase(session)
                artifacts.forEach { artifact ->
                    val bitmap =
                        assets.open(artifact.image).use { BitmapFactory.decodeStream(it) }
                    imageDatabase.addImage(artifact.image, bitmap)
                }
                config.augmentedImageDatabase = imageDatabase
            }
        }
    }

    override fun onUpdate(p0: FrameTime?) {
        val frame = arSceneFragment.arSceneView.arFrame
        val images = frame?.getUpdatedTrackables(AugmentedImage::class.java)

        if (images != null) {
            for (image in images) {
                val artifact = artifacts.firstOrNull {
                    it.image == image.name
                }
                if (artifact != null) {
                    when (image.trackingState) {
                        TrackingState.TRACKING -> {
                            //render model
                            val modelRenderable = modelRenderables[artifact.model]
                            if (modelRenderable == null && !loadingModels.contains(artifact.model)) {
                                // ensures that the model is not loading and has not been rendered
                                val anchor = image.createAnchor(image.centerPose)
                                val loadingAnchor = image.createAnchor(getVerticalPose(image.centerPose))
                                renderArtifactModel(anchor, artifact, loadingAnchor)
                            }

                            //render details card
                            val viewRenderable = viewRenderables[artifact.id.toString()]
                            if (viewRenderable == null && !loadingViews.contains(artifact.id.toString())) {
                                val imageCenterPose = image.centerPose
                                val anchor = image.createAnchor(
                                    getVerticalPose(
                                        imageCenterPose,
                                        translation = floatArrayOf(
                                            imageCenterPose.translation[0],
                                            imageCenterPose.translation[1] + (image.extentX / 2),
                                            imageCenterPose.translation[2],
                                        )
                                    )
                                )
                                renderArtifactDetailView(artifact, anchor)
                            }
                        }
                        TrackingState.PAUSED -> {}
                        TrackingState.STOPPED -> {}
                    }
                }
            }
        }
    }

    /**
     * returns a pose that is 90 degrees inclined to the face of the camera
     */
    private fun getVerticalPose(pose: Pose, translation: FloatArray? = null): Pose {
        return Pose(
            translation ?: pose.translation,
            floatArrayOf(
                pose.rotationQuaternion[0],
                90f,
                pose.rotationQuaternion[2],
                pose.rotationQuaternion[3],
            )
        )
    }

    /**
     * displays the artifacts detail card
     */
    private fun renderArtifactDetailView(artifact: Artifact, anchor: Anchor) {
        Log.d(TAG, "renderArtifactDetailView: rendering ${artifact.name}")
        loadingViews.add(artifact.id.toString())
        ViewRenderable.builder()
            .setView(this, R.layout.artifact_detail_layout)
            .build()
            .thenAccept {
                it.isShadowCaster = false
                it.isShadowReceiver = false
                it.sizer = FixedWidthViewSizer(0.3f)

                val view = it.view
                view.findViewById<TextView>(R.id.tv_artifact_title).text = artifact.name
                view.findViewById<TextView>(R.id.tv_artifact_description).text =
                    artifact.description

                placeArtifactDetailView(it, anchor, artifact)
                loadingViews.removeIf { lv ->
                    lv == artifact.id.toString()
                }
            }
            .exceptionally {
                loadingViews.removeIf { lv ->
                    lv == artifact.id.toString()
                }
                null
            }
    }

    /**
     * places the artifact detail view to its anchor position
     */
    private fun placeArtifactDetailView(
        viewRenderable: ViewRenderable,
        anchor: Anchor,
        artifact: Artifact
    ) {
        val anchorNode = AnchorNode(anchor)
        anchorNode.renderable = viewRenderable
        arSceneFragment.arSceneView.scene.addChild(anchorNode)
        viewRenderables[artifact.id.toString()] = viewRenderable
    }

    private fun showModelLoadingIndicator(artifact: Artifact, anchor: Anchor) {
        ViewRenderable.builder()
            .setView(this, R.layout.artifact_model_loading_indicator)
            .build().thenAccept {
                it.isShadowCaster = false
                it.isShadowReceiver = false
                it.sizer = FixedWidthViewSizer(0.08f)
                val anchorNode = AnchorNode(anchor)
                anchorNode.renderable = it
                arSceneFragment.arSceneView.scene.addChild(anchorNode)
                loadingIndicatorRenderables[artifact.model] = it
            }
    }

    private fun dismissModelLoadingIndicator(artifact: Artifact) {
        val loadingRenderable = loadingIndicatorRenderables[artifact.model]
        if (loadingRenderable != null) {
            val child = arSceneFragment.arSceneView.scene.children.firstOrNull {
                it.renderable == loadingRenderable
            }
            if (child != null) {
                arSceneFragment.arSceneView.scene.removeChild(child)
            }
        }
    }

    /**
     * Loads the artifact's 3d model
     */
    private fun renderArtifactModel(anchor: Anchor, artifact: Artifact, loadingAnchor: Anchor) {
        loadingModels.add(artifact.model)

        showModelLoadingIndicator(artifact, loadingAnchor)

        ModelRenderable.builder()
            .setSource(
                this, RenderableSource.builder().setSource(
                    this,
                    Uri.parse(artifact.model),
                    RenderableSource.SourceType.GLB
                )
                    .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                    .build()
            )
            .setRegistryId(artifact.model)
            .build()
            .thenAccept {
                dismissModelLoadingIndicator(artifact)
                it.isShadowReceiver = false
                it.isShadowCaster = false
                placeModel(it, anchor)

                modelRenderables[artifact.model] = it
                loadingModels.removeIf { model ->
                    model == artifact.model
                }
            }
            .exceptionally {
                Log.d(TAG, "createAnchor: Unable to load renderable $it")
                dismissModelLoadingIndicator(artifact)
                loadingModels.removeIf { model ->
                    model == artifact.model
                }
                null
            }
    }

    /**
     * places the model renderable into the desires anchor position
     */
    private fun placeModel(modelRenderable: ModelRenderable, anchor: Anchor) {
        var anchorNode: Node? = arSceneFragment.arSceneView.scene.children.firstOrNull {
            it.renderable == modelRenderable
        }
        if (anchorNode == null) {
            anchorNode = AnchorNode(anchor)
            anchorNode.renderable = modelRenderable
        }
        arSceneFragment.arSceneView.scene.addChild(anchorNode)
    }
}