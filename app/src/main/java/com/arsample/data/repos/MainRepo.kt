package com.arsample.data.repos

import android.util.Log
import com.arsample.data.models.Artifact
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

private const val TAG = "MainRepo"

class MainRepo @Inject constructor(): BaseRepo() {
    fun getArtifacts(){
        getRemoteArtifacts()
    }

    private fun getRemoteArtifacts(){
        apiService.getArtifacts().enqueue(object: Callback<ArrayList<Artifact>>{
            override fun onResponse(
                call: Call<ArrayList<Artifact>>,
                response: Response<ArrayList<Artifact>>
            ) {
                if(response.isSuccessful){
                    // TODO - handle success
                    Log.d(TAG, "getArtifacts - onResponse: Successful +${response}",)
                }
                else{
                    // TODO - handle error
                    Log.d(TAG, "getArtifacts - onResponse: Not Successful +${response}",)
                }
            }

            override fun onFailure(call: Call<ArrayList<Artifact>>, t: Throwable) {
                Log.d(TAG, "getArtifacts - onFailure:  +${t.message}",)
                // TODO - handle error
            }
        })
    }

    private fun getSampleArtifacts(){

    }
}