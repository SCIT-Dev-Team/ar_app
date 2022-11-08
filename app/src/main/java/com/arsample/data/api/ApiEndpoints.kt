package com.arsample.data.api

import com.arsample.data.models.Artifact
import retrofit2.Call
import retrofit2.http.GET

interface ApiEndpoints {
    @GET("artifacts/")
    fun getArtifacts(): Call<ArrayList<Artifact>>
}