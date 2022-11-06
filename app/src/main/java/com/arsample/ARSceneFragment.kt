package com.arsample

import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.ux.ArFragment

class ARSceneFragment : ArFragment() {

    override fun getSessionConfiguration(session: Session?): Config {
        val config = super.getSessionConfiguration(session)
        config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
        config.focusMode = Config.FocusMode.AUTO
        if (session != null) {
            (requireActivity() as MainActivity).setupDatabase(session, config)
        }
        this.arSceneView.setupSession(session)
        return config
    }
}