package com.arsample.data.repos

import com.arsample.data.api.ApiEndpoints
import com.arsample.data.api.ApiService

abstract class BaseRepo {
    val apiService = ApiService.buildService(ApiEndpoints::class.java)
}