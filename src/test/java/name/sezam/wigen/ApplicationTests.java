package name.sezam.wigen;

import name.sezam.wigen.model.Widget;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    private String getRootUrl() {
        return "http://localhost:" + port;
    }

    @Test
    public void contextLoads() {
    }

    @Test
    public void testGetAllWidgets() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);

        ResponseEntity<String> response = restTemplate.exchange(getRootUrl() + "/widgets",
                HttpMethod.GET, entity, String.class);

        Assert.assertNotNull(response.getBody());
    }

    @Test
    public void testGetWidgetById() {
        Widget widget = restTemplate.getForObject(getRootUrl() + "/widgets/1", Widget.class);
        System.out.println(widget.toString());
        Assert.assertNotNull(widget);
    }

    @Test
    public void testCreateWidget() {
        Widget widget = new Widget();
        widget.setPosX(100);
        widget.setPosY(200);
        widget.setHeight(150);
        widget.setWidth(250);

        ResponseEntity<Widget> postResponse = restTemplate.postForEntity(getRootUrl() + "/widgets", widget, Widget.class);
        Assert.assertNotNull(postResponse);
        Assert.assertNotNull(postResponse.getBody());
    }

    @Test
    public void testUpdateWidget() {
        int id = 1;
        Widget widget = restTemplate.getForObject(getRootUrl() + "/widgets/" + id, Widget.class);
        widget.setPosX(123);
        widget.setPosY(234);

        restTemplate.put(getRootUrl() + "/widgets/" + id, widget);

        Widget updatedWidget = restTemplate.getForObject(getRootUrl() + "/widgets/" + id, Widget.class);
        Assert.assertNotNull(updatedWidget);
    }

    @Test
    public void testDeleteWidget() {
        int id = 2;
        Widget widget = restTemplate.getForObject(getRootUrl() + "/widgets/" + id, Widget.class);
        Assert.assertNotNull(widget);

        restTemplate.delete(getRootUrl() + "/widgets/" + id);

        try {
            widget = restTemplate.getForObject(getRootUrl() + "/widgets/" + id, Widget.class);
        } catch (final HttpClientErrorException e) {
            Assert.assertEquals(e.getStatusCode(), HttpStatus.NOT_FOUND);
        }
    }

}
