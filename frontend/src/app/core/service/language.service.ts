import { inject, Injectable, Signal } from '@angular/core';
import { TranslocoService } from '@jsverse/transloco';
import { firstValueFrom } from 'rxjs';

import { LANGUAGES } from '../constant/language.constant';
import { Language } from '../model/language.model';

const STORAGE_KEY = 'language';

@Injectable({ providedIn: 'root' })
export class LanguageService {
  private readonly translationService = inject(TranslocoService);

  async initializeLanguage() {
    const stored = this.readStoredLanguage();
    const active = stored === null ? this.getCurrentLanguage() : stored;
    this.translationService.setActiveLang(active);
    await firstValueFrom(this.translationService.load(active));
  }

  getLanguages() {
    return LANGUAGES;
  }

  getCurrentLanguageAsSignal() {
    return this.translationService.activeLang as Signal<Language>;
  }

  getCurrentLanguage() {
    return this.translationService.getActiveLang() as Language;
  }

  async use(language: Language) {
    this.translationService.setActiveLang(language);
    this.saveLanguage(language);
    await firstValueFrom(this.translationService.load(language));
  }

  private readStoredLanguage() {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      return this.isLanguage(stored) ? stored : null;
    } catch {
      // storage unavailable (private mode, blocked): fall back to the default language
      return null;
    }
  }

  private saveLanguage(language: Language) {
    try {
      localStorage.setItem(STORAGE_KEY, language);
    } catch {
      // storage unavailable: the choice just isn't remembered
    }
  }

  private isLanguage(value: string | null) {
    return this.getLanguages().some((language) => language === value);
  }
}
