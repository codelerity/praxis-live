/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2022 Neil C Smith.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 *
 *
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 */
package org.praxislive.ide.code;

import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = CompilerOptionsQueryImplementation.class)
public class CompilerOptionsImpl implements CompilerOptionsQueryImplementation {
    
    private static final ResultImpl RESULT = new ResultImpl();

    @Override
    public Result getOptions(FileObject file) {
        var info = PathRegistry.getDefault().findInfo(file);
        if (info != null) {
            var cpp = info.project().getLookup().lookup(ClassPathProvider.class);
            if (cpp != null) {
                var modCP = cpp.findClassPath(file, JavaClassPathConstants.MODULE_COMPILE_PATH);
                if (modCP != null) {
                    return RESULT;
                }
            }
        }
        return null;
    }
    
    private static class ResultImpl extends CompilerOptionsQueryImplementation.Result {
        
        @Override
        public void addChangeListener(ChangeListener cl) {
        }

        @Override
        public List<? extends String> getArguments() {
            return List.of("--add-modules", "ALL-MODULE-PATH");
        }

        @Override
        public void removeChangeListener(ChangeListener cl) {
        }
        
    }

}
