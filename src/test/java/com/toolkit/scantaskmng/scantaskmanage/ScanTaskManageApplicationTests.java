package com.toolkit.scantaskmng.scantaskmanage;

import com.toolkit.scantaskmng.ScanTaskManageApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
//@SpringBootTest
@SpringBootTest(classes = {ScanTaskManageApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ScanTaskManageApplicationTests {

    @Test
    public void contextLoads() {
    }

}
