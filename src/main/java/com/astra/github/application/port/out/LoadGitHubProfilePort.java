package com.astra.github.application.port.out;

import com.astra.github.domain.GitHubProfile;

public interface LoadGitHubProfilePort {

    GitHubProfile viewer(String accessToken);
}
