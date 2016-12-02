package com.cargowhale.docker.client;

import com.cargowhale.docker.config.CargoWhaleProperties;
import com.cargowhale.docker.container.LogFilters;
import com.cargowhale.docker.container.info.model.ContainerDetails;
import com.cargowhale.docker.container.info.model.ContainerLogs;
import com.cargowhale.docker.container.info.model.ContainerSummary;
import com.cargowhale.docker.container.info.model.DockerContainerProcessIndex;
import com.cargowhale.docker.util.JsonConverter;
import org.assertj.core.util.Arrays;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContainerInfoClientTest {

    private static final String DOCKER_ENDPOINT = "http://this.is.docker:471828/";

    @InjectMocks
    private ContainerInfoClient client;

    @Mock
    private RestTemplate template;

    @Mock
    private DockerEndpointBuilder endpointBuilder;

    @Mock
    private JsonConverter converter;

    @Test
    public void getAllContainersReturnsEveryContainerFromDockerApi() {
        final ContainerSummary[] containerSummaryArray = Arrays.array(mock(ContainerSummary.class));

        when(this.endpointBuilder.getContainersInfoEndpoint()).thenReturn(DOCKER_ENDPOINT);
        when(this.template.getForObject(DOCKER_ENDPOINT + "?all=1", ContainerSummary[].class)).thenReturn(containerSummaryArray);

        assertThat(this.client.getAllContainers(), contains(containerSummaryArray));
    }

    @Test
    public void getFilteredContainersReturnsSelectedTypesOfContainers() {
        String filterJson = "json filter string";

        DockerContainerFilters filters = mock(DockerContainerFilters.class);
        final ContainerSummary[] containerSummaryArray = Arrays.array(mock(ContainerSummary.class));

        when(this.endpointBuilder.getContainersInfoEndpoint()).thenReturn(DOCKER_ENDPOINT);
        when(this.converter.toJson(filters)).thenReturn(filterJson);
        when(this.template.getForObject(DOCKER_ENDPOINT + "?filters={filters}", ContainerSummary[].class, filterJson))
                .thenReturn(containerSummaryArray);

        assertThat(this.client.getFilteredContainers(filters), contains(containerSummaryArray));
    }

    @Test
    public void getContainerByIdReturnsCorrectContainer() throws Exception {
        String containerId = "container_id_yo";
        ContainerDetails containerDetails = mock(ContainerDetails.class);

        when(this.endpointBuilder.getContainerInfoByIdEndpoint(containerId)).thenReturn(DOCKER_ENDPOINT + containerId);
        when(this.template.getForObject(DOCKER_ENDPOINT + containerId, ContainerDetails.class)).thenReturn(containerDetails);

        assertThat(this.client.getContainerDetailsById(containerId), is(containerDetails));
    }

    @Test
    public void getContainerLogsByIdReturnsCorrectContainerLogs() throws Exception {
        String containerId = "thisId";

        String follow = "0";
        String stdOut = "1";
        String stdErr = "1";
        String since = "0";
        String timestamps = "1";
        String tail = "265";
        LogFilters filters = new LogFilters(follow, stdOut, stdErr, since, timestamps, tail);

        String logs = "These are some fancy logs!";

        String formattedParams = String.format("?follow=%s&stdout=%s&stderr=%s&since=%s&timestamps=%s&tail=%s", follow, stdOut, stdErr, since, timestamps, tail);

        when(this.endpointBuilder.getContainerLogByIdEndpoint(containerId)).thenReturn(DOCKER_ENDPOINT + containerId);
        when(this.template.getForObject(DOCKER_ENDPOINT + containerId + formattedParams, String.class)).thenReturn(logs);

        ContainerLogs containerLogs = this.client.getContainerLogsById(containerId, filters);
        assertThat(containerLogs.getLogs(), is(logs));
    }

    @Test
    public void getContainerProcessesByIdReturnsCorrectContainerProcesses(){
        String containerId = "thisId";
        DockerContainerProcessIndex dockerIndex = Mockito.mock(DockerContainerProcessIndex.class);
        List<String> process = Lists.newArrayList("PROCESS");
        List<List<String>> processes = Lists.newArrayList();
        processes.add(process);

        when(this.endpointBuilder.getContainerProcessesByIdEndpoint(containerId)).thenReturn(DOCKER_ENDPOINT + containerId);
        when(this.template.getForObject(DOCKER_ENDPOINT + containerId, DockerContainerProcessIndex.class)).thenReturn(dockerIndex);
        when(dockerIndex.getProcesses()).thenReturn(processes);

        DockerContainerProcessIndex actual = this.client.getContainerProcessesById(containerId);
        assertThat(actual.getProcesses(), is(processes));
    }
}

