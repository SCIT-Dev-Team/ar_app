package com.arsample.ui.main

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import com.google.ar.sceneform.rendering.ModelRenderable
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
    private val renderables: HashMap<String, ModelRenderable> = HashMap()

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
                            val renderable = renderables[artifact.model]
                            if (renderable == null && !loadingModels.contains(artifact.model)) {
                                // ensures that the model is not loading and has not been rendered
                                Log.d(TAG, "onUpdate: found ${artifact.name}")
                                val anchor = image.createAnchor(image.centerPose)
                                createAnchor(anchor, artifact)
                            }
                        }
                        TrackingState.PAUSED -> {}
                        TrackingState.STOPPED -> {}
                    }
                }
            }
        }
    }

    private fun createAnchor(anchor: Anchor, artifact: Artifact) {
        Log.d(TAG, "createAnchor: creating anchor")
        loadingModels.add(artifact.model)
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
                it.isShadowReceiver = false
                it.isShadowCaster = false
                placeModel(it, anchor)

                renderables[artifact.model] = it
                loadingModels.removeIf { model ->
                    model == artifact.model
                }
                print("loaded model")
            }
            .exceptionally {
                Log.d(TAG, "createAnchor: Unable to load renderable $it")
                loadingModels.removeIf { model ->
                    model == artifact.model
                }
                null
            }
    }

    private fun placeModel(modelRenderable: ModelRenderable, anchor: Anchor) {
        var anchorNode: Node? = arSceneFragment.arSceneView.scene.children.firstOrNull {
            it.renderable == modelRenderable
        }
        if (anchorNode == null) {
            anchorNode = AnchorNode(anchor)
            anchorNode.renderable = modelRenderable
        } else {
            Log.d(TAG, "placeModel: model already placed")
        }
        arSceneFragment.arSceneView.scene.addChild(anchorNode)
    }
}