package com.cloneAll;

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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.cloneAll.GitBashHelper.cloneTheProject;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("bitbucket")
public class BitbucketCloneTest {

    @Value("${windowsDirectory}")
    private String windowsDirectory;

    @Value("${linuxDirectory}")
    private String linuxDirectory;

    @Value("${bitbucketUrl}")
    private String bitbucketUrl;

    @Value("${user}")
    private String user;

    @Value("${team}")
    private String team;

    @Value("${accessToken}")
    private String accessToken;

    @Test
    public void testCallBitbucketToClone() {
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
                    request.getHeaders().setBasicAuth(user, accessToken);
                    return execution.execute(request, body);
                })).build();

        ResponseEntity<Object> groupsResponse = restTemplate
                .exchange(bitbucketUrl + "/rest/api/latest/projects/" + team + "/repos?start=0&limit=100", HttpMethod.GET, null, Object.class);

        assertEquals(groupsResponse.getStatusCode(), HttpStatus.OK);

        Map<String, Object> responseBody = (Map<String, Object>) groupsResponse.getBody();

        ArrayList<Map<String, Object>> values = (ArrayList<Map<String, Object>>) responseBody.get("values");

        values.stream()
                .forEach(value -> {
                    Map<String, Object> links = (Map<String, Object>) value.get("links");
                    List<Map<String, String>> clone = (List<Map<String, String>>) links.get("clone");
                    Optional<Map<String, String>> collect = clone.stream()
                            .filter(stringStringMap -> stringStringMap.get("href").startsWith("ssh://"))
                            .findFirst();

                    if (collect.isPresent()) {
                        projectsSSHLink.add(collect.get().get("href"));
                    }
                });

        return projectsSSHLink;
    }
}
