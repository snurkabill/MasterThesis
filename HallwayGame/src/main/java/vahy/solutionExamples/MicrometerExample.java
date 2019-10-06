package vahy.solutionExamples;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.lang.Nullable;
import io.micrometer.elastic.ElasticConfig;
import io.micrometer.elastic.ElasticMeterRegistry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

import static junit.framework.Assert.assertTrue;

public class MicrometerExample {

    public static void main(String[] args) {

        ElasticConfig elasticConfig = new ElasticConfig() {
            @Override
            @Nullable
            public String get(String k) {
                if(k.equals("elastic.step")) {
                    return Duration.ofSeconds(10).toString();
                }
                return null;
            }
        };

        MeterRegistry registry = new ElasticMeterRegistry(elasticConfig, Clock.SYSTEM);
        Timer myTimer = registry.timer("my.timer.new");
        Counter counter = registry.counter("my.timer.new.count");

        List<String> list = new ArrayList<>(4);

        Gauge gauge = Gauge
            .builder("cache.size", list, List::size)
            .register(registry);

        assertTrue(gauge.value() == 0.0);

        list.add("1");

        assertTrue(gauge.value() == 1.0);

        list.add("128");
        assertTrue(gauge.value() == 2.0);


//        new JvmGcMetrics().bindTo(registry);
//        new JvmMemoryMetrics().bindTo(registry);

        for (int i = 0; i < 1000000; i++) {
            System.out.println(i);
            MyWorker myWorker = new MyWorker(new SplittableRandom().nextInt(10, 50));
            var runnable = myTimer.wrap(myWorker);
            runnable.run();
            counter.increment();
        }


    }

    public static double square(double x) {
        return x * x;
    }




}
