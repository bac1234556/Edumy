export const DEFAULT_VIDEO_URL = 'https://www.w3schools.com/html/mov_bbb.mp4';
export const DEFAULT_PDF_URL = 'https://www.w3schools.com/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf';

/**
 * Build lesson creation payload shared by CourseCreate and CourseCurriculumEditor.
 * Converts durationMinutes -> duration (seconds) and resolves videoUrl based on contentType.
 */
export function buildLessonPayload(lessonForm, orderIndex) {
  const contentType = lessonForm.contentType || 'VIDEO';
  const trimmedTitle = (lessonForm.title || '').trim();
  const rawVideoUrl = (lessonForm.videoUrl || '').trim();

  let resolvedVideoUrl = '';
  if (contentType === 'VIDEO') {
    resolvedVideoUrl = rawVideoUrl || DEFAULT_VIDEO_URL;
  } else if (contentType === 'PDF') {
    resolvedVideoUrl = rawVideoUrl || DEFAULT_PDF_URL;
  }

  return {
    title: trimmedTitle,
    videoUrl: resolvedVideoUrl,
    content: (lessonForm.content || '').trim() || `[${contentType}] ${trimmedTitle}`,
    duration: Math.max(1, parseInt(lessonForm.durationMinutes, 10) || 5) * 60,
    isPreview: Boolean(lessonForm.isPreview),
    orderIndex
  };
}
