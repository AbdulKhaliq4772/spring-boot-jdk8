package gateway.middlewarewebservice.entity;

import pk.vaulsys.apigateway.util.Util;

import java.io.Serializable;

@Entity
@Table(name = "PRIVACY_POLICY")
public class PrivacyPolicy implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "privacy_policy_seq")
    @SequenceGenerator(name = "privacy_policy_seq", sequenceName = "PRIVACY_POLICY_ID_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "LANG", length = 3, nullable = false)
    private String lang;

    @Lob
    @Column(name = "DESCRIPTION")
    private String description;

    public PrivacyPolicy() {
    }

    public PrivacyPolicy(String lang, String description) {
        this.lang = lang;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return Util.toJSON(this);
    }
}