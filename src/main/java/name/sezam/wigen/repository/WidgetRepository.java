package name.sezam.wigen.repository;

import name.sezam.wigen.model.Widget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * The interface Widget repository.
 *
 * @author sezam
 */
@Repository
public interface WidgetRepository extends JpaRepository<Widget, Long> {
}
