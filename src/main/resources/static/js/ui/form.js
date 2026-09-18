const formUtil = (() => {
  function toJson(formEl) {
    const data = {};
    new FormData(formEl).forEach((value, key) => {
      data[key] = value;
    });
    return data;
  }

  function clearErrors(formEl) {
    formEl.querySelectorAll('.field').forEach((field) => {
      field.querySelector('.input')?.classList.remove('has-error');
      const errorEl = field.querySelector('.error-text');
      if (errorEl) errorEl.remove();
    });
  }

  /** Applies an ApiError's validationErrors map onto matching name="" fields. */
  function applyValidationErrors(formEl, validationErrors) {
    clearErrors(formEl);
    if (!validationErrors) return;
    Object.entries(validationErrors).forEach(([fieldName, message]) => {
      const input = formEl.querySelector(`[name="${fieldName}"]`);
      if (!input) return;
      input.classList.add('has-error');
      const field = input.closest('.field') || input.parentElement;
      const error = document.createElement('div');
      error.className = 'error-text';
      error.textContent = (typeof i18n !== 'undefined' && i18n.translateFieldError) ? i18n.translateFieldError(message) : message;
      field.appendChild(error);
    });
  }

  return { toJson, clearErrors, applyValidationErrors };
})();
