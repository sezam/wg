package name.sezam.wigen.controller;

import com.google.common.util.concurrent.RateLimiter;
import name.sezam.wigen.exception.ResourceNotFoundException;
import name.sezam.wigen.model.Widget;
import name.sezam.wigen.repository.WidgetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.validation.constraints.Max;
import java.util.*;

/**
 * Widget controller.
 *
 * @author sezam
 */
@RestController
@RequestMapping("/api")
public class WidgetController {

    @Autowired
    private WidgetRepository widgetRepository;

    private List<Widget> list;

    @Value("${wigen.rate_limit}")
    private Integer rateLimit;

    final RateLimiter rateLimiter;

    public WidgetController() {
        rateLimiter = RateLimiter.create(rateLimit / 60);
        list = widgetRepository.findAll();
    }

    private List<Widget> getList() {
        List<Widget> nl = new ArrayList<Widget>();
        Collections.copy(nl, list);
        return nl;
    }


    /**
     * Get all widgets list.
     *
     * @return the list
     */
    @GetMapping("/widgets")
    public List<Widget> getAllWidgets(@RequestParam(required = false, defaultValue = "0") Integer page,
                                      @RequestParam(required = false, defaultValue = "10") @Max(500) Integer size,
                                      @RequestParam(required = false) Integer ax1,
                                      @RequestParam(required = false) Integer ay1,
                                      @RequestParam(required = false) Integer ax2,
                                      @RequestParam(required = false) Integer ay2,
                                      @RequestParam(required = false) Integer rateLimit,
                                      HttpServletResponse response)
            throws ResourceNotFoundException {

        response.addHeader("RateLimit", String.valueOf(rateLimiter.getRate() * 60));
        response.addHeader("RateUse", String.valueOf(rateLimiter.acquire() * 60));

        if (rateLimit != null) {
            rateLimiter.setRate(rateLimit / 60);
        }

        List<Widget> filteredList = getList();
        if (ax1 != null && ay1 != null && ax2 != null && ay2 != null) {
            filteredList.forEach(w -> {
                if (w.getPosX() < ax1
                        || w.getPosY() < ay1
                        || w.getPosX() + w.getWidth() > ax2
                        || w.getPosY() + w.getHeight() > ay2)
                    filteredList.remove(w);
            });
        }

        if (page != null && size != null && size > 0) {
            if (page > filteredList.size() / size - 1)
                throw new ResourceNotFoundException("No data for page :: " + page);
            return filteredList.subList(filteredList.size() / size - 1, size);
        }
        return filteredList;
    }

    /**
     * Gets widgets by id.
     *
     * @param widgetId the widget id
     * @return the widgets by id
     * @throws ResourceNotFoundException the resource not found exception
     */
    @GetMapping("/widgets/{id}")
    public ResponseEntity<Widget> getWidgetById(@PathVariable(value = "id") Long widgetId, HttpServletResponse response)
            throws ResourceNotFoundException {

        response.addHeader("RateLimit", String.valueOf(rateLimiter.getRate() * 60));
        response.addHeader("RateUse", String.valueOf(rateLimiter.acquire() * 60));

        Widget widget = getList().get(widgetId.intValue());
        if (widget == null) throw new ResourceNotFoundException("Widget not found on :: " + widgetId);
        return ResponseEntity.ok().body(widget);
    }

    /**
     * Create widget.
     *
     * @param widgetDetails the widget
     * @return the widget
     */
    @PostMapping("/widgets")
    @Transactional
    public Widget createWidget(@RequestBody Widget widgetDetails, HttpServletResponse response) {
        widgetDetails.setUpdateAt(new Date());
        if (widgetDetails.getZOrder() == null) widgetDetails.setZOrder(0);

        response.addHeader("RateLimit", String.valueOf(rateLimiter.getRate() * 60));
        response.addHeader("RateUse", String.valueOf(rateLimiter.acquire() * 60));

        Widget widget = widgetRepository.save(widgetDetails);
        getList().add(widget.getZOrder(), widget);

        getList().forEach(w -> {
            w.setZOrder(getList().indexOf(w));
        });
        widgetRepository.saveAll(getList());
        widgetRepository.flush();

        return widget;
    }

    /**
     * Update widget.
     *
     * @param widgetId      the widget id
     * @param widgetDetails the widget details
     * @return the response entity
     * @throws ResourceNotFoundException the resource not found exception
     */
    @PutMapping("/widgets/{id}")
    @Transactional
    public ResponseEntity<Widget> updateWidget(
            @PathVariable(value = "id") Long widgetId, @RequestBody Widget widgetDetails, HttpServletResponse response)
            throws ResourceNotFoundException {

        widgetDetails.setUpdateAt(new Date());
        if (widgetDetails.getZOrder() == null) widgetDetails.setZOrder(0);

        response.addHeader("RateLimit", String.valueOf(rateLimiter.getRate() * 60));
        response.addHeader("RateUse", String.valueOf(rateLimiter.acquire() * 60));

        Widget widget =
                widgetRepository
                        .findById(widgetId)
                        .orElseThrow(() -> new ResourceNotFoundException("Widget not found on :: " + widgetId));

        widget.setPosX(widgetDetails.getPosX());
        widget.setPosY(widgetDetails.getPosY());
        widget.setWidth(widgetDetails.getWidth());
        widget.setHeight(widgetDetails.getHeight());
        widget.setZOrder(widgetDetails.getZOrder());

        final Widget updatedWidget = widgetRepository.save(widget);

        if (widget.getZOrder() != widgetDetails.getZOrder()) {
            getList().add(updatedWidget.getZOrder(), updatedWidget);

            getList().forEach(w -> {
                w.setZOrder(getList().indexOf(w));
            });
            widgetRepository.saveAll(getList());
            widgetRepository.flush();
        }

        return ResponseEntity.ok(updatedWidget);
    }

    /**
     * Delete widget map.
     *
     * @param widgetId the widget id
     * @return the map
     * @throws Exception the exception
     */
    @DeleteMapping("/widgets/{id}")
    @Transactional
    public Map<String, Boolean> deleteWidget(@PathVariable(value = "id") Long widgetId, HttpServletResponse response) throws Exception {
        response.addHeader("RateLimit", String.valueOf(rateLimiter.getRate() * 60));
        response.addHeader("RateUse", String.valueOf(rateLimiter.acquire() * 60));

        Widget widget =
                widgetRepository
                        .findById(widgetId)
                        .orElseThrow(() -> new ResourceNotFoundException("Widget not found at :: " + widgetId));

        getList().forEach(w -> {
            if (w.getId().compareTo(widgetId) == 0) getList().remove(w);
        });
        widgetRepository.delete(widget);
        widgetRepository.flush();

        Map<String, Boolean> resp = new HashMap<>();
        resp.put("deleted", Boolean.TRUE);
        return resp;
    }
}
