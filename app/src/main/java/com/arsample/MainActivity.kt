package com.arsample

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.rendering.ModelRenderable

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), Scene.OnUpdateListener {
    private lateinit var arSceneFragment: ARSceneFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        arSceneFragment =
            supportFragmentManager.findFragmentById(R.id.ar_fragment) as ARSceneFragment
        arSceneFragment.arSceneView.scene.addOnUpdateListener(this)
    }

    fun setupDatabase(session: Session, config: Config) {
        val imageDatabase = AugmentedImageDatabase(session)
        val cubeBitmap = assets.open("cube.jpg").use { BitmapFactory.decodeStream(it) }
        imageDatabase.addImage("cube", cubeBitmap)
        config.augmentedImageDatabase = imageDatabase
    }

    override fun onUpdate(p0: FrameTime?) {
        val frame = arSceneFragment.arSceneView.arFrame
        var images = frame?.getUpdatedTrackables(AugmentedImage::class.java)

        if (images != null) {
            for (image in images){
                when(image.trackingState){
                    TrackingState.TRACKING -> {
                        val anchor = image.createAnchor(image.centerPose)
                        if(image.name.equals("cube")){
                            Log.d(TAG, "onUpdate: Frame tracking - cube identified")
                            createAnchor(anchor)
                        }
                    }
                    TrackingState.PAUSED -> {}
                    TrackingState.STOPPED -> {}
                }
            }
        }
    }

    private fun createAnchor(anchor: Anchor) {
//        ModelRenderable.builder()
//            .setSource(this, Uri.parse("models/rubiks_cube.glb"))
//            .build()
//            .thenAccept {
//                placeModel(it, anchor)
//            }
    }

    private fun placeModel(modelRenderable: ModelRenderable, anchor: Anchor) {
        val anchorNode = AnchorNode(anchor)
        anchorNode.renderable = modelRenderable
        arSceneFragment.arSceneView.scene.addChild(anchorNode)
    }
}