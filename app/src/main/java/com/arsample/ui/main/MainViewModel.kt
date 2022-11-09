package com.arsample.ui.main

import androidx.lifecycle.ViewModel
import com.arsample.data.models.Artifact
import com.arsample.data.repos.MainRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val mainRepo: MainRepo) : ViewModel() {
    fun getArtifacts(): Flow<ArrayList<Artifact>> {
        return mainRepo.getArtifacts()
    }
}