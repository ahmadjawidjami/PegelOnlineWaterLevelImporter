package de.tu_berlin.ise.open_data.waterlevel.batch;


import de.tu_berlin.ise.open_data.library.batch.*;
import de.tu_berlin.ise.open_data.waterlevel.config.ResourceProperties;
import de.tu_berlin.ise.open_data.waterlevel.model.WaterLevel;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Created by ahmadjawid on 7/2/17.
 */
@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ResourceProperties resourceProperties;


    @Bean
    ItemReader<WaterLevel> itemReader() {
        return new WaterLevelCustomJsonItemReader(resourceProperties.getUrl(), restTemplate);
    }

    @Bean
    ItemProcessor<WaterLevel, String> itemProcessor() {
        return new WaterLevelItemProcessor();
    }

    @Bean
    ItemWriter<String> itemWriter() {
        return new JsonItemWriter();
    }

    @Bean
    public StepProcessListener stepExecutionListener() {
        return new StepProcessListener();
    }

    @Bean
    Step step1() {
        return stepBuilderFactory.get("step1").listener(stepExecutionListener())
                .<WaterLevel, String>chunk(100)
                .reader(itemReader())
                .processor(itemProcessor())
                .writer(itemWriter())
                .build();
    }

    @Bean
    Job waterLevelJob(JobCompletionNotificationListener jobCompletionNotificationListener) {
        return jobBuilderFactory.get("waterLevelJob")
                .incrementer(new RunIdIncrementer())
                .listener(jobCompletionNotificationListener)
                .flow(step1())
                .end()
                .build();
    }
}