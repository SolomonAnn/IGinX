/*
 * IGinX - the polystore system with high performance
 * Copyright (C) Tsinghua University
 * TSIGinX@gmail.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package cn.edu.tsinghua.iginx.engine.shared.function.udf.python;

import cn.edu.tsinghua.iginx.engine.shared.function.Function;
import java.util.concurrent.BlockingQueue;
import pemja.core.PythonInterpreter;

public abstract class PyUDF implements Function {

  protected final BlockingQueue<PythonInterpreter> interpreters;

  protected final String moduleName;

  public PyUDF(BlockingQueue<PythonInterpreter> interpreters, String moduleName) {
    this.interpreters = interpreters;
    this.moduleName = moduleName;
  }

  public void close() {
    while (!interpreters.isEmpty()) {
      PythonInterpreter interpreter = interpreters.poll();
      if (interpreter != null) {
        // remove the module
        interpreter.exec(String.format("import sys; sys.modules.pop('%s', None)", moduleName));
        interpreter.close();
      }
    }
  }
}