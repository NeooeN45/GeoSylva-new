package com.forestry.counter.presentation.screens.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forestry.counter.domain.model.Project
import com.forestry.counter.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ProjectsViewModel(
    private val projectRepository: ProjectRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProjectsUiState>(ProjectsUiState.Loading)
    val uiState: StateFlow<ProjectsUiState> = _uiState.asStateFlow()

    init {
        loadProjects()
    }

    private fun loadProjects() {
        viewModelScope.launch {
            projectRepository.getAll().collect { projects ->
                _uiState.value = if (projects.isEmpty()) {
                    ProjectsUiState.Empty
                } else {
                    ProjectsUiState.Success(projects)
                }
            }
        }
    }

    fun createProject(name: String, description: String?, color: String?) {
        viewModelScope.launch {
            val project = Project(
                projectId = UUID.randomUUID().toString(),
                name = name,
                description = description,
                color = color,
            )
            projectRepository.insert(project)
        }
    }

    fun toggleFavorite(projectId: String, currentFavorite: Boolean) {
        viewModelScope.launch {
            projectRepository.setFavorite(projectId, !currentFavorite)
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            projectRepository.delete(projectId)
        }
    }
}

sealed interface ProjectsUiState {
    data object Loading : ProjectsUiState
    data object Empty : ProjectsUiState
    data class Success(val projects: List<Project>) : ProjectsUiState
}
