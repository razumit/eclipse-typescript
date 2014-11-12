/*
 * Copyright 2013 Palantir Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.typescript.text.actions;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Shell;

import com.palantir.typescript.EclipseResources;
import com.palantir.typescript.services.language.TextSpan;
import com.palantir.typescript.text.RenameRefactoringWizard;
import com.palantir.typescript.text.TypeScriptEditor;
import com.palantir.typescript.text.TypeScriptRenameProcessor;

/**
 * The rename action allows renaming various TypeScript elements.
 *
 * @author dcicerone
 */
public final class RenameAction extends TypeScriptEditorAction {

    public RenameAction(TypeScriptEditor editor) {
        super(editor);
    }

    @Override
    public boolean isEnabled() {
        TypeScriptEditor editor = this.getTextEditor();

        // non-workspace files cannot be refactored
        if (!EclipseResources.isEclipseFile(editor.getFileName())) {
            return false;
        }

        return super.isEnabled();
    }

    @Override
    public void run() {
        TypeScriptEditor editor = this.getTextEditor();
        ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();
        int offset = selection.getOffset();
        String oldName = this.getOldName(offset);
        RefactoringProcessor processor = new TypeScriptRenameProcessor(editor.getLanguageService(), offset, oldName);
        ProcessorBasedRefactoring refactoring = new ProcessorBasedRefactoring(processor);
        RenameRefactoringWizard wizard = new RenameRefactoringWizard(refactoring);
        RefactoringWizardOpenOperation operation = new RefactoringWizardOpenOperation(wizard);
        Shell shell = editor.getSite().getShell();

        try {
            operation.run(shell, "");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String getOldName(int offset) {
        TypeScriptEditor editor = this.getTextEditor();
        TextSpan span = editor.getLanguageService().getNameOrDottedNameSpan(offset, offset);

        try {
            String oldName = editor.getDocument().get(span.getStart(), span.getLength());

            int lastPeriod = oldName.lastIndexOf('.');
            if (lastPeriod >= 0) {
                oldName = oldName.substring(lastPeriod + 1);
            }

            return oldName;
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }
}
