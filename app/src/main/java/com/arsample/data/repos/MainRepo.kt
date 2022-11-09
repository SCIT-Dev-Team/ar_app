package com.arsample.data.repos

import android.util.Log
import com.arsample.data.models.Artifact
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

private const val TAG = "MainRepo"

class MainRepo @Inject constructor() : BaseRepo() {
    fun getArtifacts(): Flow<ArrayList<Artifact>> {
        return getSampleArtifacts()
    }

    private fun getRemoteArtifacts() {
        apiService.getArtifacts().enqueue(object : Callback<ArrayList<Artifact>> {
            override fun onResponse(
                call: Call<ArrayList<Artifact>>,
                response: Response<ArrayList<Artifact>>
            ) {
                if (response.isSuccessful) {
                    // TODO - handle success
                    Log.d(TAG, "getArtifacts - onResponse: Successful +${response}")
                } else {
                    // TODO - handle error
                    Log.d(TAG, "getArtifacts - onResponse: Not Successful +${response}")
                }
            }

            override fun onFailure(call: Call<ArrayList<Artifact>>, t: Throwable) {
                Log.d(TAG, "getArtifacts - onFailure:  +${t.message}")
                // TODO - handle error
            }
        })
    }

    private fun getSampleArtifacts(): Flow<ArrayList<Artifact>> = flow {
        val artifacts: ArrayList<Artifact> = ArrayList()
        artifacts.add(
            Artifact(
                1,
                "T77 Handgun",
                "T77 Handgun description",
                "images/t77_handgun.png",
                "https://res.cloudinary.com/dq1q7jztv/image/upload/v1667977816/AR/t77_handgun_xuysq5.glb"
            )
        )
        artifacts.add(
            Artifact(
                2,
                "Dragon Rigged",
                "Dragon Rigged description",
                "images/dragon_rigged.png",
                "https://res.cloudinary.com/dq1q7jztv/image/upload/v1667977816/AR/dragon_rigged_mms60o.glb"
            )
        )
        emit(artifacts)
    }
}