package com.example.imagesubstitution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.ImageNameSubstitutor;

/**
 * Custom Testcontainers image substitutor that integrates with settings in testcontainers.properties.
 * This class ensures that the Ryuk resource reaper uses its official Docker Hub image
 * (testcontainers/ryuk) while allowing the hub.image.name.prefix (ghcr.io/mrayandutta/postgres-demo/)
 * to be applied to all other images by the default PrefixingImageNameSubstitutor.
 * 
 * Key Behavior:
 * - If the image contains "testcontainers/ryuk", it is reverted to "testcontainers/ryuk:<original-tag>",
 *   overriding any prior prefixing to ensure Ryuk works correctly from Docker Hub.
 * - For all other images (e.g., postgres:15), this substitutor leaves them unchanged, relying on
 *   the PrefixingImageNameSubstitutor (configured via hub.image.name.prefix) to prepend the prefix.
 * 
 * Configuration Dependency:
 * - Works in conjunction with testcontainers.properties:
 *   - hub.image.name.prefix=ghcr.io/mrayandutta/postgres-demo/ (applies prefix to non-Ryuk images)
 *   - docker.image.substitutor.enabled=true (enables image substitution)
 *   - image.substitutor=com.example.imagesubstitution.CustomImageSubstitutor (registers this class)
 * 
 * Note: This substitutor runs after the default PrefixingImageNameSubstitutor in the substitution chain,
 * allowing it to correct Ryuk’s image name if needed.
 */
public class CustomImageSubstitutor extends ImageNameSubstitutor {
    private static final Logger log = LoggerFactory.getLogger(CustomImageSubstitutor.class);

    @Override
    public DockerImageName apply(DockerImageName original) {
        log.debug("Processing image substitution for: {}", original);
        DockerImageName finalDockerImageName = original;

        String unversionedPart = original.getUnversionedPart();

        // Handle Ryuk: revert to original if prefixed incorrectly
        if (unversionedPart.contains("testcontainers/ryuk")) {
        	finalDockerImageName = DockerImageName.parse("testcontainers/ryuk")
                .withTag(original.getVersionPart());
            log.info("Correcting Ryuk image substitution: {} → {}", original, finalDockerImageName);
        }

        return finalDockerImageName;
    }

    @Override
    protected String getDescription() {
        return "CustomImageSubstitutor (excludes Ryuk, prefixes others)";
    }
}