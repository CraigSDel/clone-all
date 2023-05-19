package com.cloneAll;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.cloneAll.GitBashHelper.cloneTheProject;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("gitlab")
public class GitlabCloneTest {

    @Value("${windowsDirectory}")
    private String windowsDirectory;

    @Value("${linuxDirectory}")
    private String linuxDirectory;

    @Value("${gitlabUrl}")
    private String gitlabUrl;

    @Value("${accessToken}")
    private String accessToken;

    @Test
    public void callGitlabToClone() {
        List<String> projectsSSHLink = getProjectsSSHLink();
        projectsSSHLink.stream().forEach(sshLink -> {
            try {
                cloneTheProject(sshLink, windowsDirectory, linuxDirectory);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private List<String> getProjectsSSHLink() {
        List<String> projectsSSHLink = new ArrayList<>();

        RestTemplate restTemplate = new RestTemplateBuilder(rt -> rt.getInterceptors()
                .add((request, body, execution) -> {
                    request.getHeaders().add("PRIVATE-TOKEN", accessToken);
                    return execution.execute(request, body);
                })).build();

        ResponseEntity<Object> groupsResponse = restTemplate
                .exchange(gitlabUrl + "/api/v4/groups", HttpMethod.GET, null, Object.class);

        assertEquals(groupsResponse.getStatusCode(), HttpStatus.OK);

        List<LinkedHashMap<String, Object>> groups = (List<LinkedHashMap<String, Object>>) groupsResponse.getBody();

        Assertions.assertNotNull(groups);

        groups.stream().forEach(project -> {
            Integer id = (Integer) project.get("id");
            String url = gitlabUrl + "/api/v4/groups/" + id + "?include_subgroups=true";
            ResponseEntity<Object> repoResponse = restTemplate
                    .exchange(url, HttpMethod.GET, null, Object.class);

            Assertions.assertNotNull(repoResponse.getBody());

            LinkedHashMap<String, Object> repoResponseBody = (LinkedHashMap<String, Object>) repoResponse.getBody();
            List<Map<String, Object>> projectsList = (List<Map<String, Object>>) repoResponseBody.get("projects");

            projectsList.forEach(stringObjectMap -> {
                projectsSSHLink.add((String) stringObjectMap.get("ssh_url_to_repo"));
            });
        });

        Assertions.assertNotEquals(0, projectsSSHLink.size());
        return projectsSSHLink;
    }
}
