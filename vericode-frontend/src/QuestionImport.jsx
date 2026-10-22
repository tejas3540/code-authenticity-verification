import { lazy, Suspense } from "react";

const QuestionImportContent = lazy(() => import("./QuestionImportContent.jsx"));

function ImportSkeleton() {
  return (
    <div className="question-import-skeleton" aria-hidden="true">
      <span />
    </div>
  );
}

export default function QuestionImport() {
  return (
    <Suspense fallback={<ImportSkeleton />}>
      <QuestionImportContent />
    </Suspense>
  );
}
