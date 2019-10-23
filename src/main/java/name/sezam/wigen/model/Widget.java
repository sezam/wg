package name.sezam.wigen.model;

import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * The type Widget.
 *
 * @author sezam
 */
@Entity
@Data
@Table(name = "widgets")
@EntityListeners(AuditingEntityListener.class)
public class Widget implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(name = "posX", nullable = false)
    private Integer posX;

    @NotNull
    @Column(name = "posY", nullable = false)
    private Integer posY;

    @NotNull
    @Column(name = "width", nullable = false)
    private Integer width;

    @NotNull
    @Column(name = "height", nullable = false)
    private Integer height;

    @Column(name = "zOrder", nullable = false)
    private Integer zOrder;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = false)
    private Date updateAt;
}
