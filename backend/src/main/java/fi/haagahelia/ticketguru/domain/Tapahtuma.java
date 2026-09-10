package fi.haagahelia.ticketguru.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Tapahtuma {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nimi;

    @Column(nullable = false)
    private LocalDateTime aika;

    @Column(nullable = false, length = 80)
    private String kaupunki;

    @Column(nullable = false, length = 120)
    private String paikka;

    @Column(nullable = false)
    private Integer lippujaKpl;

    @OneToMany(mappedBy = "tapahtuma")
    private List<Lipputyyppi> lipputyypit = new ArrayList<>();

    @OneToMany(mappedBy = "tapahtuma")
    private List<Myyntitapahtuma> myyntitapahtumat = new ArrayList<>();

    public Tapahtuma() {
    }

    public Tapahtuma(String nimi, LocalDateTime aika, String kaupunki, String paikka, Integer lippujaKpl) {
        this.nimi = nimi;
        this.aika = aika;
        this.kaupunki = kaupunki;
        this.paikka = paikka;
        this.lippujaKpl = lippujaKpl;
    }

    public Long getId() {
        return id;
    }

    public String getNimi() {
        return nimi;
    }

    public void setNimi(String nimi) {
        this.nimi = nimi;
    }

    public LocalDateTime getAika() {
        return aika;
    }

    public void setAika(LocalDateTime aika) {
        this.aika = aika;
    }

    public String getKaupunki() {
        return kaupunki;
    }

    public void setKaupunki(String kaupunki) {
        this.kaupunki = kaupunki;
    }

    public String getPaikka() {
        return paikka;
    }

    public void setPaikka(String paikka) {
        this.paikka = paikka;
    }

    public Integer getLippujaKpl() {
        return lippujaKpl;
    }

    public void setLippujaKpl(Integer lippujaKpl) {
        this.lippujaKpl = lippujaKpl;
    }

    public List<Lipputyyppi> getLipputyypit() {
        return lipputyypit;
    }

    public void setLipputyypit(List<Lipputyyppi> lipputyypit) {
        this.lipputyypit = lipputyypit;
    }

    public List<Myyntitapahtuma> getMyyntitapahtumat() {
        return myyntitapahtumat;
    }

    public void setMyyntitapahtumat(List<Myyntitapahtuma> myyntitapahtumat) {
        this.myyntitapahtumat = myyntitapahtumat;
    }
}
