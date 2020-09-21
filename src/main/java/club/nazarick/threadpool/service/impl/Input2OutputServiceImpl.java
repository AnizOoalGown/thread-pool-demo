package club.nazarick.threadpool.service.impl;

import club.nazarick.threadpool.domain.Input;
import club.nazarick.threadpool.domain.Output;
import club.nazarick.threadpool.service.Input2OutputService;
import club.nazarick.threadpool.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

@Service
@Slf4j
public class Input2OutputServiceImpl implements Input2OutputService {
    @Override
    public Output singleProcess(Input input) {
        log.info("Processing...");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return new Output(false, null);
        }
        return new Output(true, String.valueOf(2 * input.getI() + 1));
    }

    @Override
    public List<Output> multiProcess(List<Input> inputList) {
        ThreadPoolTaskExecutor executor
                = SpringUtils.getBean("threadPoolTaskExecutor", ThreadPoolTaskExecutor.class);
        CountDownLatch latch = new CountDownLatch(inputList.size());
        List<Output> outputList = Collections.synchronizedList(new ArrayList<>(inputList.size()));

        for (Input input : inputList) {
            executor.execute(() -> {
                try {
                    Output output = singleProcess(input);
                    outputList.add(output);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return outputList;
    }

    @Async("threadPoolTaskExecutor")
    @Override
    public Future<Output> asyncProcess(Input input) {
        return new AsyncResult<>(singleProcess(input));
    }
}
