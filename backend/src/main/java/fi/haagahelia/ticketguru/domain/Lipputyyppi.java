package fi.haagahelia.ticketguru.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class Lipputyyppi {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 80)
    private String kuvaus;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal hinta;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tapahtuma_id", nullable = false)
    private Tapahtuma tapahtuma;

    @OneToMany(mappedBy = "lipputyyppi")
    private List<Lippu> liput = new ArrayList<>();

    public Lipputyyppi() {
    }

    public Lipputyyppi(String kuvaus, BigDecimal hinta, Tapahtuma tapahtuma) {
        this.kuvaus = kuvaus;
        this.hinta = hinta;
        this.tapahtuma = tapahtuma;
    }

    public Long getId() {
        return id;
    }

    public String getKuvaus() {
        return kuvaus;
    }

    public void setKuvaus(String kuvaus) {
        this.kuvaus = kuvaus;
    }

    public BigDecimal getHinta() {
        return hinta;
    }

    public void setHinta(BigDecimal hinta) {
        this.hinta = hinta;
    }

    public Tapahtuma getTapahtuma() {
        return tapahtuma;
    }

    public void setTapahtuma(Tapahtuma tapahtuma) {
        this.tapahtuma = tapahtuma;
    }

    public List<Lippu> getLiput() {
        return liput;
    }

    public void setLiput(List<Lippu> liput) {
        this.liput = liput;
    }
}
