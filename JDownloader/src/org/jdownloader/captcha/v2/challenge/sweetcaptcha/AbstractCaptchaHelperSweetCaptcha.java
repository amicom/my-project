package org.jdownloader.captcha.v2.challenge.sweetcaptcha;

import java.util.logging.Logger;

import jd.http.Browser;
import jd.plugins.Plugin;

import org.appwork.utils.Regex;
import org.jdownloader.logging.LogController;

public abstract class AbstractCaptchaHelperSweetCaptcha<T extends Plugin> {
    protected T       plugin;
    protected Logger  logger;
    protected Browser br;
    protected String  siteKey;
    protected String  appKey;

    public AbstractCaptchaHelperSweetCaptcha(T plugin, Browser br, final String siteKey, final String appKey) {
        this.plugin = plugin;
        this.br = br;
        logger = plugin.getLogger();
        if (logger == null) {
            logger = LogController.getInstance().getLogger(getClass().getSimpleName());
        }
        this.siteKey = siteKey;
        this.appKey = appKey;
    }

    public T getPlugin() {
        return plugin;
    }

    /**
     *
     *
     * @author raztoki
     * @since JD2
     * @return
     */
    public String getSweetCaptchaApiKey() {
        return getSweetCaptchaApiKey(br != null ? br.toString() : null);
    }

    /**
     * will auto find api key, based on google default &lt;div&gt;, @Override to make customised finder.
     *
     * @author raztoki
     * @since JD2
     * @return
     */
    public static String getSweetCaptchaApiKey(final String source) {
        if (source == null) {
            return null;
        }
        String apiKey = new Regex(source, "<div[^>]+id=(?:\"|'|)(sc_[a-f0-9]{7})").getMatch(0);
        return apiKey;
    }

    /**
     *
     *
     * @author raztoki
     * @since JD2
     * @return
     */
    public String getSweetCaptchaAppKey() {
        return getSweetCaptchaAppKey(br != null ? br.toString() : null);
    }

    /**
     * will auto find api key, based on google default &lt;div&gt;, @Override to make customised finder.
     *
     * @author raztoki
     * @since JD2
     * @return
     */
    public static String getSweetCaptchaAppKey(final String source) {
        if (source == null) {
            return null;
        }
        String apiKey = new Regex(source, "sweetcaptcha\\.com/api/v2/apps/(\\d+)/captcha/sc_").getMatch(0);
        return apiKey;
    }

}
