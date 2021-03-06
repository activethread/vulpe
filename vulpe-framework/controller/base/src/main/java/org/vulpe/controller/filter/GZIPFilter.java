/**
 * Vulpe Framework - Quick and Smart ;)
 * Copyright (C) 2011 Active Thread
 *
 * Este programa é software livre; você pode redistribuí-lo e/ou
 * modificá-lo sob os termos da Licença Pública Geral GNU, conforme
 * publicada pela Free Software Foundation; tanto a versão 2 da
 * Licença como (a seu critério) qualquer versão mais nova.
 *
 * Este programa é distribuído na expectativa de ser útil, mas SEM
 * QUALQUER GARANTIA; sem mesmo a garantia implícita de
 * COMERCIALIZAÇÃO ou de ADEQUAÇÃO A QUALQUER PROPÓSITO EM
 * PARTICULAR. Consulte a Licença Pública Geral GNU para obter mais
 * detalhes.
 *
 * Você deve ter recebido uma cópia da Licença Pública Geral GNU
 * junto com este programa; se não, escreva para a Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
/**
 * Vulpe Framework - Quick and Smart ;)
 * Copyright (C) 2011 Active Thread
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.vulpe.controller.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

@WebFilter(filterName = "gzip", urlPatterns = "/*")
public class GZIPFilter implements Filter {

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (request instanceof HttpServletRequest) {
			final HttpServletRequest httpRequest = (HttpServletRequest) request;
			final HttpServletResponse httpResponse = (HttpServletResponse) response;
			final String acceptEncoding = httpRequest.getHeader("accept-encoding");
			if (acceptEncoding != null && acceptEncoding.indexOf("gzip") != -1) {
				final GZIPResponseWrapper wrappedResponse = new GZIPResponseWrapper(httpResponse);
				chain.doFilter(httpRequest, wrappedResponse);
				wrappedResponse.finishResponse();
				return;
			}
			chain.doFilter(httpRequest, httpResponse);
		}
	}

	public void init(FilterConfig filterConfig) {
		// noop
	}

	public void destroy() {
		// noop
	}
}

/*
 * Copyright 2003 Jayson Falkner (jayson@jspinsider.com) This code is from
 * "Servlets and JavaServer pages; the J2EE Web Tier", http://www.jspbook.com.
 * You may freely use the code both commercially and non-commercially. If you
 * like the code, please pick up a copy of the book and help support the
 * authors, development of more free code, and the JSP/Servlet/J2EE community.
 */
class GZIPResponseStream extends ServletOutputStream {
	private ByteArrayOutputStream baos = null;
	private GZIPOutputStream gzipstream = null;
	private boolean closed = false;
	private HttpServletResponse response = null;
	private ServletOutputStream output = null;

	public GZIPResponseStream(HttpServletResponse response) throws IOException {
		super();
		closed = false;
		this.response = response;
		this.output = response.getOutputStream();
		baos = new ByteArrayOutputStream();
		gzipstream = new GZIPOutputStream(baos);
	}

	public void close() throws IOException {
		if (closed) {
			throw new IOException("This output stream has already been closed");
		}
		gzipstream.finish();

		byte[] bytes = baos.toByteArray();

		response.addHeader("Content-Length", Integer.toString(bytes.length));
		response.addHeader("Content-Encoding", "gzip");
		output.write(bytes);
		output.flush();
		output.close();
		closed = true;
	}

	public void flush() throws IOException {
		if (closed) {
			throw new IOException("Cannot flush a closed output stream");
		}
		gzipstream.flush();
	}

	public void write(int b) throws IOException {
		if (closed) {
			throw new IOException("Cannot write to a closed output stream");
		}
		gzipstream.write((byte) b);
	}

	public void write(byte b[]) throws IOException {
		write(b, 0, b.length);
	}

	public void write(byte b[], int off, int len) throws IOException {
		if (closed) {
			throw new IOException("Cannot write to a closed output stream");
		}
		gzipstream.write(b, off, len);
	}

	public boolean closed() {
		return (this.closed);
	}

	public void reset() {
		// noop
	}
}

/*
 * Copyright 2003 Jayson Falkner (jayson@jspinsider.com) This code is from
 * "Servlets and JavaServer pages; the J2EE Web Tier", http://www.jspbook.com.
 * You may freely use the code both commercially and non-commercially. If you
 * like the code, please pick up a copy of the book and help support the
 * authors, development of more free code, and the JSP/Servlet/J2EE community.
 */
class GZIPResponseWrapper extends HttpServletResponseWrapper {
	protected HttpServletResponse origResponse = null;
	protected ServletOutputStream stream = null;
	protected PrintWriter writer = null;

	public GZIPResponseWrapper(HttpServletResponse response) {
		super(response);
		origResponse = response;
	}

	public ServletOutputStream createOutputStream() throws IOException {
		return (new GZIPResponseStream(origResponse));
	}

	public void finishResponse() {
		try {
			if (writer != null) {
				writer.close();
			} else {
				if (stream != null) {
					stream.close();
				}
			}
		} catch (IOException e) {
		}
	}

	public void flushBuffer() throws IOException {
		stream.flush();
	}

	public ServletOutputStream getOutputStream() throws IOException {
		if (writer != null) {
			throw new IllegalStateException("getWriter() has already been called!");
		}

		if (stream == null)
			stream = createOutputStream();
		return (stream);
	}

	public PrintWriter getWriter() throws IOException {
		if (writer != null) {
			return (writer);
		}

		if (stream != null) {
			throw new IllegalStateException("getOutputStream() has already been called!");
		}

		stream = createOutputStream();
		writer = new PrintWriter(new OutputStreamWriter(stream, "UTF-8"));
		return (writer);
	}

	public void setContentLength(int length) {
	}

}
