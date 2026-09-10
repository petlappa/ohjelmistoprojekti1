package fi.haagahelia.ticketguru.domain;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Lippu {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String koodi;

    @Column(nullable = false)
    private boolean kaytetty = false;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal hinta;

    @ManyToOne(optional = false)
    @JoinColumn(name = "myyntitapahtuma_id", nullable = false)
    private Myyntitapahtuma myyntitapahtuma;

    @ManyToOne(optional = false)
    @JoinColumn(name = "lipputyyppi_id", nullable = false)
    private Lipputyyppi lipputyyppi;

    public Lippu() {
    }

    public Lippu(String koodi, BigDecimal hinta, Myyntitapahtuma myyntitapahtuma, Lipputyyppi lipputyyppi) {
        this.koodi = koodi;
        this.hinta = hinta;
        this.myyntitapahtuma = myyntitapahtuma;
        this.lipputyyppi = lipputyyppi;
    }

    public Long getId() {
        return id;
    }

    public String getKoodi() {
        return koodi;
    }

    public void setKoodi(String koodi) {
        this.koodi = koodi;
    }

    public boolean isKaytetty() {
        return kaytetty;
    }

    public void setKaytetty(boolean kaytetty) {
        this.kaytetty = kaytetty;
    }

    public BigDecimal getHinta() {
        return hinta;
    }

    public void setHinta(BigDecimal hinta) {
        this.hinta = hinta;
    }

    public Myyntitapahtuma getMyyntitapahtuma() {
        return myyntitapahtuma;
    }

    public void setMyyntitapahtuma(Myyntitapahtuma myyntitapahtuma) {
        this.myyntitapahtuma = myyntitapahtuma;
    }

    public Lipputyyppi getLipputyyppi() {
        return lipputyyppi;
    }

    public void setLipputyyppi(Lipputyyppi lipputyyppi) {
        this.lipputyyppi = lipputyyppi;
    }
}
