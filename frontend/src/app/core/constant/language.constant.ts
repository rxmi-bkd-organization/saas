import type { Language } from '../model/language.model';

export const LANGUAGES = ['en', 'fr'] as const;

export const LANGUAGE_LABELS: Record<Language, string> = {
  en: 'English',
  fr: 'Français',
};
