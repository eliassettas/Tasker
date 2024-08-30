package com.example.tasker.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.tasker.exception.CustomException;
import com.example.tasker.model.dto.TypeDTO;
import com.example.tasker.model.persistence.JobTitle;
import com.example.tasker.repository.JobTitleRepository;

@Service
public class JobTitleService {

    private final JobTitleRepository jobTitleRepository;

    public JobTitleService(JobTitleRepository jobTitleRepository) {
        this.jobTitleRepository = jobTitleRepository;
    }

    public List<TypeDTO> getAllJobTitles() {
        return jobTitleRepository.findAll().stream()
                .map(TypeDTO::fromObject)
                .collect(Collectors.toList());
    }

    public JobTitle getJobTitleByName(String name) throws CustomException {
        Optional<JobTitle> jobTitle = jobTitleRepository.findByName(name);
        return jobTitle.orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Job Title not found"));
    }
}
