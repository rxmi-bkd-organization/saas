import { ChangeDetectionStrategy, Component, effect, inject, signal } from '@angular/core';
import { form, FormField } from '@angular/forms/signals';
import { LANGUAGE_LABELS } from '../../../core/constant/language.constant';
import { LanguageForm } from '../../../core/model/form/language.form';
import { LanguageService } from '../../../core/service/language.service';

@Component({
  selector: 'app-language-selector',
  imports: [FormField],
  templateUrl: './language-selector.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LanguageSelector {
  private readonly languageService = inject(LanguageService);

  private readonly model = signal<LanguageForm>({ language: this.languageService.getCurrentLanguage() });

  protected readonly options = this.languageService.getLanguages().map((language) => ({
    value: language,
    label: LANGUAGE_LABELS[language],
  }));

  protected readonly languageForm = form(this.model);

  constructor() {
    effect(() => {
      const { language } = this.model();
      if (language !== this.languageService.getCurrentLanguage()) this.languageService.use(language);
    });
  }
}
