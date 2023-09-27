package com.example.demo.service;

import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.ScalableResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;


import java.io.*;
import java.nio.charset.StandardCharsets;


@Service
public class KubernetesService {

    public void deployJobFromYaml(String apiServer, String token) {
        Config config = new ConfigBuilder()
                .withMasterUrl(apiServer)
                .withOauthToken(token)
                .withNamespace("default")
                .withTrustCerts(true)
                .build();

        try (KubernetesClient kubernetesClient = new DefaultKubernetesClient(config)) {
            // Load and create the Job object from the YAML configuration String
            String yamlTemplate = loadYamlTemplateFromFile();
            String yamlContent = yamlTemplate.replace("${nodeName}", "worker1");
            System.out.println(yamlContent);

            // Debugging: Print the YAML content before parsing
            System.out.println("YAML Content Before Parsing:");
            System.out.println(yamlContent);

            InputStream inputStream = new ByteArrayInputStream(yamlContent.getBytes(StandardCharsets.UTF_8));

            // Debugging: Check if the inputStream is not null
            System.out.println("Input Stream: " + inputStream);

            ScalableResource<Job> scalableResource = kubernetesClient.batch().jobs().load(inputStream);
            System.out.println(scalableResource);
            Job job = scalableResource.createOrReplace();

            // Debugging: Print the job object to check if it's null
            System.out.println("Job Object: " + job);

            if (job != null) {
                // Create the Job resource in the Kubernetes cluster
                //kubernetesClient.batch().v1().jobs().inNamespace("default").createOrReplace(job);

                System.out.println("Job created successfully.");
            } else {
                System.out.println("Job object is null.");
            }
        } catch (KubernetesClientException e) {
            e.printStackTrace();
            System.out.println("KubernetesClientException: " + e.getMessage());
            // Handle Kubernetes client errors
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String loadYamlTemplateFromFile() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/application.yml")) {
            byte[] bytes = StreamUtils.copyToByteArray(inputStream);
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }
}
