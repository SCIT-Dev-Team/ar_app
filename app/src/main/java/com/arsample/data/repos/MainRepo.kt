package com.arsample.data.repos

import com.arsample.data.models.Artifact
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainRepo: BaseRepo() {
    fun getArtifacts(){
        apiService.getArtifacts().enqueue(object: Callback<ArrayList<Artifact>>{
            override fun onResponse(
                call: Call<ArrayList<Artifact>>,
                response: Response<ArrayList<Artifact>>
            ) {
                if(response.isSuccessful){
                    // TODO - handle success
                }
                else{
                    // TODO - handle error
                }
            }

            override fun onFailure(call: Call<ArrayList<Artifact>>, t: Throwable) {
                // TODO - handle error
            }
        })
    }
}