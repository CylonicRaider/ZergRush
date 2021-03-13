package net.zergrush.xml;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSException;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSSerializer;

public abstract class XMLIO {

    public static class DefaultXMLIO extends XMLIO {

        public static class DOMErrorException extends IOException {

            private static final long serialVersionUID =
                -5749227290801328263L;

            private final DOMError source;

            public DOMErrorException(DOMError source) {
                super(formatMessage(source),
                      prepareCause(source.getRelatedException()));
                this.source = source;
            }

            public DOMError getSource() {
                return source;
            }

            public static String formatMessage(DOMError err) {
                String severity;
                switch (err.getSeverity()) {
                    case DOMError.SEVERITY_WARNING:
                        severity = "WARNING";
                        break;
                    case DOMError.SEVERITY_ERROR:
                        severity = "ERROR";
                        break;
                    case DOMError.SEVERITY_FATAL_ERROR:
                        severity = "FATAL ERROR";
                        break;
                    default:
                        severity = "ERROR WITH UNRECOGNIZED SEVERITY(?!)";
                        break;
                }
                return severity + " at " + err.getLocation().getLineNumber() +
                    ':' + err.getLocation().getColumnNumber() + ": " +
                    err.getMessage();
            }

            private static Throwable prepareCause(Object exception) {
                return (exception instanceof Throwable) ?
                    (Throwable) exception : null;
            }

        }

        public static class StoringErrorHandler implements DOMErrorHandler {

            private DOMErrorException error;

            public DOMErrorException getError() {
                return error;
            }

            public boolean handleError(DOMError error) {
                if (error.getSeverity() == DOMError.SEVERITY_WARNING)
                    return true;
                // Storing a wrapping exception instead of the error allows us
                // to (hopefully) capture all the gory stack trace details.
                if (this.error == null)
                    this.error = new DOMErrorException(error);
                // Behold this *exceptionally wise* design decision: "If an
                // exception is thrown from this method, it is considered to
                // be equivalent of returning true [i.e. continuing
                // processing]." I'm not sure if the upstream DOM spec's
                // stipulation that handleError() throw no exception is a just
                // cause for this requirement, or deserves the same
                // condemnation as implied above.
                return false;
            }

            public <T extends Throwable> T augment(T t) {
                // We expect the pertinent exceptions not to have a cause;
                // however, it might be more appropriate to add the
                // DOMErrorException as a suppressed exception instead anyway.
                if (error != null) t.initCause(error);
                return t;
            }

        }

        public static final DefaultXMLIO DEFAULT = new DefaultXMLIO();

        private final DOMImplementation impl;

        public DefaultXMLIO(DOMImplementation impl) {
            if (! (impl instanceof DOMImplementationLS))
                throw new IllegalArgumentException(
                    "DOM Load-Save required but not available");
            this.impl = impl;
        }
        public DefaultXMLIO() {
            this(getDefaultImplementation());
        }

        public Document createDocument(String rootNodeName) {
            return impl.createDocument(null, rootNodeName, null);
        }

        protected void configure(DOMConfiguration config,
                                 StoringErrorHandler eh,
                                 boolean namespaceAware) throws DOMException {
            config.setParameter("error-handler", eh);
            config.setParameter("namespaces", namespaceAware);
        }

        public Document read(Reader source, boolean namespaceAware)
                throws IOException {
            LSInput input = ((DOMImplementationLS) impl).createLSInput();
            input.setCharacterStream(source);
            StoringErrorHandler eh = new StoringErrorHandler();
            try {
                LSParser p = ((DOMImplementationLS) impl).createLSParser(
                    DOMImplementationLS.MODE_SYNCHRONOUS, null);
                configure(p.getDomConfig(), eh, namespaceAware);
                return p.parse(input);
            } catch (DOMException exc) {
                throw new IOException(eh.augment(exc));
            } catch (LSException exc) {
                throw new IOException(eh.augment(exc));
            }
        }

        public void write(Document doc, Writer drain, boolean namespaceAware)
                throws IOException {
            LSOutput output = ((DOMImplementationLS) impl).createLSOutput();
            output.setCharacterStream(drain);
            StoringErrorHandler eh = new StoringErrorHandler();
            try {
                LSSerializer s = ((DOMImplementationLS) impl)
                    .createLSSerializer();
                configure(s.getDomConfig(), eh, namespaceAware);
                if (! s.write(doc, output))
                    throw eh.augment(new IOException(
                        "Could not serialize document"));
            } catch (DOMException exc) {
                throw new IOException(eh.augment(exc));
            } catch (LSException exc) {
                throw new IOException(eh.augment(exc));
            }
        }

        public static DOMImplementation getDefaultImplementation() {
            DOMImplementation impl;
            try {
                impl = DOMImplementationRegistry.newInstance()
                    .getDOMImplementation("XML 3.0 LS 3.0");
            } catch (ClassNotFoundException exc) {
                throw new RuntimeException(exc);
            } catch (InstantiationException exc) {
                throw new RuntimeException(exc);
            } catch (IllegalAccessException exc) {
                throw new RuntimeException(exc);
            }
            return impl;
        }

    }

    public abstract Document createDocument(String rootNodeName);

    public abstract Document read(Reader source,
                                  boolean namespaceAware) throws IOException;

    public abstract void write(Document doc, Writer drain,
                               boolean namespaceAware) throws IOException;

    public Document readString(String source, boolean namespaceAware)
            throws IOException {
        return read(new StringReader(source), namespaceAware);
    }

    public String writeString(Document doc, boolean namespaceAware)
            throws IOException {
        StringWriter wr = new StringWriter();
        write(doc, wr, namespaceAware);
        return wr.toString();
    }

    public static XMLIO getDefault() {
        return DefaultXMLIO.DEFAULT;
    }

}
