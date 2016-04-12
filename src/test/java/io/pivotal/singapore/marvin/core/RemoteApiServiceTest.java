package io.pivotal.singapore.marvin.core;

import io.pivotal.singapore.marvin.utils.a;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;

import static junit.framework.TestCase.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class RemoteApiServiceTest {

    @Test
    public void testRemoteCommandCollaboration() {
        assertTrue(
            a.remoteApiService.build()
                .call(a.command.build(), new HashMap<String, String>())
                .isSuccessful()
        );
    }
}
