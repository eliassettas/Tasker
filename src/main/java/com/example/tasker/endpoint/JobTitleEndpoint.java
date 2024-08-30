package com.example.tasker.endpoint;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.tasker.model.dto.TypeDTO;
import com.example.tasker.service.JobTitleService;

@RestController
@RequestMapping("api/job-titles")
public class JobTitleEndpoint {

    private final JobTitleService jobTitleService;

    public JobTitleEndpoint(JobTitleService jobTitleService) {
        this.jobTitleService = jobTitleService;
    }

    @GetMapping
    public List<TypeDTO> getAllJobTitles() {
        return jobTitleService.getAllJobTitles();
    }
}
