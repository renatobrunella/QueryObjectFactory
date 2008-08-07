/*
 * Copyright 2008 brunella ltd
 *
 * Licensed under the LGPL Version 3 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package sf.qof.bundle;

import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Internal - Helper class to track OSGi logging service.
 */
public class Logger {

  public static final int LOG_ERROR = LogService.LOG_ERROR;
  public static final int LOG_WARNING = LogService.LOG_WARNING;
  public static final int LOG_INFO  = LogService.LOG_INFO;
  public static final int LOG_DEBUG = LogService.LOG_DEBUG;

  protected ServiceTracker tracker;

  public Logger(BundleContext context) {
    tracker = new ServiceTracker(context, LogService.class.getName(), null);
  }
  
  public void open() {
    tracker.open();
  }
  
  public void close() {
    tracker.close();
  }
  
  public void log(int level, String message) {
    LogService logService = (LogService) tracker.getService();
    if (logService != null) {
      logService.log(level, message);
    }
  }
  
  public void log(int level, String message, Throwable exception) {
    LogService logService = (LogService) tracker.getService();
    if (logService != null) {
      logService.log(level, message, exception);
    }
  }
}