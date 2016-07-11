package ar.edu.itba.pdc.utils;

import ar.edu.itba.pdc.connection.Server;
import ar.edu.itba.pdc.transformations.ImageRotation;
import ar.edu.itba.pdc.transformations.L33tTransformation;
import ar.edu.itba.pdc.transformations.NoneTransformation;
import ar.edu.itba.pdc.transformations.Transformation;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public enum ProxyConfiguration {
    INSTANCE;

    private static final Logger LOGGER = Logger.getLogger(ProxyConfiguration.class);

    private ExecutorService threadPool;

    private Properties properties = null;
    private String configPath;

    private static final String CLIENT_PORT = "client_port";
    private static final String SERVER_PORT = "server_port";
    private static final String ADMIN_PORT = "admin_port";
    private static final String KEY_DEFAULT_SERVER = "default_server";
    private static final String KEY_DEFAULT_SERVER_PORT = "default_server_port";
    private static final String KEY_BUFFER_SIZE = "buffer_size";
    private static final String KEY_USER = "user";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_THREAD = "pool_thread_size";
    private static final String KEY_MAIL_SIZE = "image_limit_size_mb";

    private static final String DEFAULT_CONFIG_FILE = "default.properties";
    private Server defaultServer;

    private Transformation subjectTransformation = NoneTransformation.INSTANCE;
    private Transformation imageTransformation = NoneTransformation.INSTANCE;

    private int maxImageLengthMb;
    private int threadPoolSize;

    public void init(final String configPropertiesPath) {
        if (configPropertiesPath == null || configPropertiesPath.isEmpty()) {
            configPath = DEFAULT_CONFIG_FILE;
        } else {
            configPath = configPropertiesPath;
        }

        properties = new Properties();
        InputStream input = null;
        try {
            File config = new File(configPath);
            if (!config.exists()) {
                initDefaultConfigurationFile();
            }
            input = new FileInputStream(configPath);
            properties.load(input);
            if (!checkAllProperties()) {
                properties = null;
                System.out.println("Failed to load properties file.");
            } else {
                defaultServer = new Server(getConfig(KEY_DEFAULT_SERVER), Integer.parseInt(getConfig(KEY_DEFAULT_SERVER_PORT)));
                threadPoolSize = Integer.parseInt(getConfig(KEY_THREAD));
                maxImageLengthMb = Integer.parseInt(getConfig(KEY_MAIL_SIZE));
                threadPool = Executors.newFixedThreadPool(threadPoolSize);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void initDefaultConfigurationFile() {
        try {
            properties = new Properties();
            FileOutputStream output = new FileOutputStream(configPath);
            properties.setProperty(KEY_USER, "leet_pop3_proxy_server");
            properties.setProperty(KEY_PASSWORD, "leet_pop3_proxy_server");
            properties.setProperty(KEY_BUFFER_SIZE, "2048");
            properties.setProperty(CLIENT_PORT, "1337");
            properties.setProperty(SERVER_PORT, "1338");
            properties.setProperty(ADMIN_PORT, "1339");
            properties.setProperty(KEY_DEFAULT_SERVER, "localhost");
            properties.setProperty(KEY_DEFAULT_SERVER_PORT, "110");
            properties.setProperty(KEY_THREAD, "10");
            properties.setProperty(KEY_MAIL_SIZE, "100");
            properties.store(output, null);
        } catch (Exception e) {
            System.out.println("Failed to load initial configuration values.");
            e.printStackTrace();
        }
    }

    public void setConfigurationPath(final String path) {
        Properties currentProperties = properties;
        this.configPath = path;
        init(path);
        this.properties = currentProperties;
        try {
            saveProperties();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkAllProperties() {
        if (properties.get(KEY_USER) == null)
            return false;
        if (properties.get(KEY_PASSWORD) == null)
            return false;
        if (properties.get(CLIENT_PORT) == null)
            return false;
        if (properties.get(SERVER_PORT) == null)
            return false;
        if (properties.get(ADMIN_PORT) == null)
            return false;
        if (properties.get(KEY_BUFFER_SIZE) == null)
            return false;
        if (properties.get(KEY_DEFAULT_SERVER) == null)
            return false;
        if (properties.get(KEY_DEFAULT_SERVER_PORT) == null)
            return false;
        if (properties.get(KEY_THREAD) == null)
            return false;
        if (properties.get(KEY_MAIL_SIZE) == null)
            return false;
        return true;
    }

    public String getUser() {
        return getConfig(KEY_USER).split("\n")[0];
    }

    public void setUser(final String user) {
        if (user == null) {
            throw new IllegalArgumentException();
        }
        setConfig(KEY_USER, user);
    }

    public String getPassword() {
        return getConfig(KEY_PASSWORD).split("\n")[0];
    }

    public void setPassword(final String password) {
        if (password == null) {
            throw new IllegalArgumentException();
        }
        setConfig(KEY_PASSWORD, password);
        return;
    }

    public int getClientPort() {
        return Integer.parseInt(getConfig(CLIENT_PORT));
    }

    public int getServerPort() {
        return Integer.parseInt(getConfig(SERVER_PORT));
    }

    public int getAdminPort() {
        return Integer.parseInt(getConfig(ADMIN_PORT));
    }

    public int getBufferSize() {
        return Integer.parseInt(getConfig(KEY_BUFFER_SIZE));
    }

    private String getConfig(final String key) {
        return properties.getProperty(key);
    }

    public Server getKeyDefaultServer() {
        return this.defaultServer;
    }

    private boolean setConfig(final String key, final String value) {
        try {
            properties.setProperty(key, value);
            saveProperties();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void saveProperties() throws IOException {
        final FileOutputStream output = new FileOutputStream(configPath);
        properties.store(output, null);
        output.close();
    }

    public Transformation subjectTransformation() {
        return subjectTransformation;
    }

    public void setLeetSubjectTransformation() {
        this.subjectTransformation = L33tTransformation.INSTANCE;
        LOGGER.info("L33T transformation ON.");
    }

    public void desactivateSubjectTransformation() {
        this.subjectTransformation = NoneTransformation.INSTANCE;
        LOGGER.info("Subject transformation OFF.");
    }

    public Transformation imageTransformation() {
        return imageTransformation;
    }

    public void setImageRotationTransformation() {
        this.imageTransformation = ImageRotation.INSTANCE;
        LOGGER.info("Image rotation ON.");
    }

    public void desactivateImageTransformation() {
        this.imageTransformation = NoneTransformation.INSTANCE;
        LOGGER.info("Image transformation OFF.");
    }

    public int maxImageLengthMb() {
        return this.maxImageLengthMb;
    }

    public ExecutorService getThreadPool() {
        return this.threadPool;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }
}
