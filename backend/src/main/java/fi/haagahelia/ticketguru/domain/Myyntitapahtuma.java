package fi.haagahelia.ticketguru.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
public class Myyntitapahtuma {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime myyntiaika;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal summa;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tapahtuma_id", nullable = false)
    private Tapahtuma tapahtuma;

    @ManyToOne(optional = false)
    @JoinColumn(name = "myyja_id", nullable = false)
    private Kayttaja myyja;

    @OneToMany(mappedBy = "myyntitapahtuma")
    private List<Lippu> liput = new ArrayList<>();

    public Myyntitapahtuma() {
    }

    public Myyntitapahtuma(LocalDateTime myyntiaika, BigDecimal summa, Tapahtuma tapahtuma, Kayttaja myyja) {
        this.myyntiaika = myyntiaika;
        this.summa = summa;
        this.tapahtuma = tapahtuma;
        this.myyja = myyja;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getMyyntiaika() {
        return myyntiaika;
    }

    public void setMyyntiaika(LocalDateTime myyntiaika) {
        this.myyntiaika = myyntiaika;
    }

    public BigDecimal getSumma() {
        return summa;
    }

    public void setSumma(BigDecimal summa) {
        this.summa = summa;
    }

    public Tapahtuma getTapahtuma() {
        return tapahtuma;
    }

    public void setTapahtuma(Tapahtuma tapahtuma) {
        this.tapahtuma = tapahtuma;
    }

    public Kayttaja getMyyja() {
        return myyja;
    }

    public void setMyyja(Kayttaja myyja) {
        this.myyja = myyja;
    }

    public List<Lippu> getLiput() {
        return liput;
    }

    public void setLiput(List<Lippu> liput) {
        this.liput = liput;
    }
}
