import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { FieldTree, FormField as FormFieldDirective } from '@angular/forms/signals';

@Component({
  selector: 'app-form-field',
  imports: [FormFieldDirective],
  templateUrl: './form-field.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FormField {
  readonly field = input.required<FieldTree<string>>();
  readonly label = input.required<string>();
  readonly type = input<'text' | 'email' | 'password'>('text');
}
