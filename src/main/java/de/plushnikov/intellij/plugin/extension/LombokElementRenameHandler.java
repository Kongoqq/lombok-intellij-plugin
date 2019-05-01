package de.plushnikov.intellij.plugin.extension;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.refactoring.actions.BaseRefactoringAction;
import com.intellij.refactoring.rename.PsiElementRenameHandler;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import com.intellij.refactoring.rename.inplace.InplaceRefactoring;
import com.intellij.refactoring.rename.inplace.MemberInplaceRenameHandler;
import de.plushnikov.intellij.plugin.psi.LombokLightFieldBuilder;
import de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * "Rename"-Handler to support methods generated by lombok
 */
public class LombokElementRenameHandler extends MemberInplaceRenameHandler {

  @Override
  protected boolean isAvailable(PsiElement element, Editor editor, PsiFile file) {
    return super.isAvailable(element, editor, file) &&
      ((element instanceof LombokLightMethodBuilder || element instanceof LombokLightFieldBuilder)
        && !(element.getNavigationElement() instanceof PsiAnnotation));
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, PsiFile file, DataContext dataContext) {
    PsiElement element = PsiElementRenameHandler.getElement(dataContext);
    if (null == element) {
      element = BaseRefactoringAction.getElementAtCaret(editor, file);
    }

    if (null != element) {
      RenamePsiElementProcessor processor = RenamePsiElementProcessor.forElement(element);
      element = processor.substituteElementToRename(element, editor);
    }

    if (null != element) {
      editor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
      PsiElement nameSuggestionContext = InjectedLanguageUtil.findElementAtNoCommit(file, editor.getCaretModel().getOffset());
      PsiElementRenameHandler.invoke(element, project, nameSuggestionContext, editor);
    }
  }

  @Override
  public InplaceRefactoring doRename(@NotNull PsiElement elementToRename, @NotNull Editor editor, @Nullable DataContext dataContext) {
    RenamePsiElementProcessor processor = RenamePsiElementProcessor.forElement(elementToRename);
    PsiElement actualElem = processor.substituteElementToRename(elementToRename, editor);
    return super.doRename(actualElem, editor, dataContext);
  }
}
