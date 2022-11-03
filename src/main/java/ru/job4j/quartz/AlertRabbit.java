package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {

    private static Properties getProperties() {
        var properties = new Properties();
        try (var in = AlertRabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    public static void main(String[] args) throws ClassNotFoundException {
        var config = getProperties();
        Class.forName(config.getProperty("driver-class-name"));
        try (var cn = DriverManager.getConnection(
                config.getProperty("url"),
                config.getProperty("username"),
                config.getProperty("password"))) {
            var scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            var data = new JobDataMap();
            data.put("connection", cn);
            var job = newJob(Rabbit.class)
                    .usingJobData(data)
                    .build();

            var times = simpleSchedule()
                    .withIntervalInSeconds(Integer.parseInt(config.getProperty("rabbit.interval")))
                    .repeatForever();

            var trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();

            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class Rabbit implements Job {

        private final LocalDateTime created;

        public Rabbit() {
            created = LocalDateTime.now();
        }

        public LocalDateTime getCreated() {
            return created;
        }

        @Override
        public void execute(JobExecutionContext context) {
            var cn = (Connection) context.getJobDetail().getJobDataMap().get("connection");
            try (var statement =
                         cn.prepareStatement("insert into rabbit(created_date) values (?)")) {
                statement.setTimestamp(1, Timestamp.valueOf(getCreated()));
                statement.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}