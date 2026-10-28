import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { FieldTree, FormRoot } from '@angular/forms/signals';

@Component({
  selector: 'app-form-container',
  imports: [FormRoot],
  templateUrl: './form-container.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FormContainer {
  readonly form = input.required<FieldTree<unknown>>();
  readonly heading = input.required<string>();
  readonly submitLabel = input.required<string>();
  readonly disabled = input(false);

  protected readonly submitDisabled = computed(() => this.disabled() || this.form()().submitting());
}
