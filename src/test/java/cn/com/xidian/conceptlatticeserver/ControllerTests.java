package cn.com.xidian.conceptlatticeserver;

import cn.com.xidian.conceptlatticeserver.controller.ServerController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@WebMvcTest(ServerController.class)
@AutoConfigureRestDocs(outputDir = "docs/")
public class ControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldReturnDefaultGraph() {
        mockMvc.perform(ge)
    }
}
