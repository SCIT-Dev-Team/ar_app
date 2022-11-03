package com.arsample

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    private lateinit var arFragment: ArFragment
    private  lateinit var modelRenderable: ModelRenderable
    private var modelUrl = "https://github.com/josephmusila/AR-Sample/blob/main/ar_asset.glb"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFragment = supportFragmentManager.findFragmentById(R.id.fragment) as ArFragment
        setupModel()
        setupPlane()
    }

    private fun setupPlane(){
        arFragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
            var anchor = hitResult.createAnchor()
            var anchorNode = AnchorNode(anchor)
            anchorNode.setParent(arFragment.arSceneView.scene)
            createModel(anchorNode)
        }
    }

    private fun createModel(anchorNode: AnchorNode) {
        var node = TransformableNode(arFragment.transformationSystem)
        node.setParent(anchorNode)
        node.renderable = modelRenderable
        node.select()
    }

    private fun setupModel(){
        ModelRenderable.builder()
            .setSource(this,
                RenderableSource.builder().setSource(this, Uri.parse(modelUrl), RenderableSource.SourceType.GLB)
                    .setScale(0.75f)
                    .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                    .build()
            ).build()
            .thenAccept {
                modelRenderable = it
                Log.d(TAG, "setupModel: Then Accept")
            }

    }
}