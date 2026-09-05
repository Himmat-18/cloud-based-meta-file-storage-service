import { useState } from "react";

function UploadFile({ onClose,folderId }) {
  const [selectedFile, setSelectedFile] = useState(null);
  const [progress, setProgress] = useState(0);
  const [isUploading, setIsUploading] = useState(false);
  const [message, setMessage] = useState("");
  const [isError, setIsError] = useState(false);
  const [isDragging, setIsDragging] = useState(false);

  const handleFileSelect = (file) => {
    if (!file) return;

    setSelectedFile(file);
    setProgress(0);
    setMessage("");
    setIsError(false);
  };

  const handleInputChange = (event) => {
    const file = event.target.files[0];
    handleFileSelect(file);
  };

  const handleDrop = (event) => {
    event.preventDefault();
    setIsDragging(false);

    const file = event.dataTransfer.files[0];
    handleFileSelect(file);
  };

  const handleDragOver = (event) => {
    event.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = () => {
    setIsDragging(false);
  };

  const startUpload = () => {
  if (!selectedFile) return;

  const userId = localStorage.getItem("userId");
  const token = localStorage.getItem("token");

  if (!userId) {
    setMessage("User ID not found. Please login again.");
    setIsError(true);
    return;
  }

  if (!token) {
    setMessage("Authentication token not found. Please login again.");
    setIsError(true);
    return;
  }

  setIsUploading(true);
  setProgress(0);
  setMessage("");
  setIsError(false);

  const formData = new FormData();

  formData.append("file", selectedFile);

  if (folderId !== null && folderId !== undefined){
    formData.append("folderId",folderId);
  }
  

  const xhr = new XMLHttpRequest();

  xhr.open(
    "POST",
    "http://localhost:8080/api/files/upload"
  );

  xhr.setRequestHeader(
    "Authorization",
    `Bearer ${token}`
  );

  xhr.upload.onprogress = (event) => {
    if (event.lengthComputable) {
      const percent = Math.round(
        (event.loaded / event.total) * 100
      );

      setProgress(percent);
    }
  };

  xhr.onload = () => {
    setIsUploading(false);

    if (xhr.status >= 200 && xhr.status < 300) {
      setProgress(100);
      setMessage("File uploaded successfully!");
      setIsError(false);
    } else {
      console.log("Upload response:", xhr.responseText);

      setMessage(
        "Upload failed. Backend returned status " +
          xhr.status
      );
      setIsError(true);
    }
  };

  xhr.onerror = () => {
    setIsUploading(false);
    setIsError(true);
    setMessage(
      "Cannot connect to Spring Boot backend."
    );
  };

  xhr.send(formData);
};

  const getFilePreview = () => {
    if (!selectedFile) return null;

    if (selectedFile.type.startsWith("image/")) {
      return (
        <img
          src={URL.createObjectURL(selectedFile)}
          alt="Preview"
          className="max-h-64 max-w-full rounded-lg object-contain mx-auto"
        />
      );
    }

    if (selectedFile.type === "application/pdf") {
      return (
        <iframe
          src={URL.createObjectURL(selectedFile)}
          title="PDF Preview"
          className="w-full h-64 rounded-lg border"
        />
      );
    }

    return (
      <div className="text-center py-8">
        <div className="text-5xl mb-3">
          📄
        </div>

        <p className="font-medium text-gray-800">
          {selectedFile.name}
        </p>

        <p className="text-sm text-gray-500 mt-1">
          Preview is not available for this file type
        </p>
      </div>
    );
  };

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-lg p-6">

        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-xl font-bold text-gray-800">
            Upload File
          </h2>

          <button
            onClick={onClose}
            className="text-gray-500 hover:text-gray-800 text-2xl"
          >
            ×
          </button>
        </div>

        {/* Drag and Drop */}
        {!selectedFile && (
          <div
            onDrop={handleDrop}
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            className={`border-2 border-dashed rounded-xl p-10 text-center transition ${
              isDragging
                ? "border-blue-500 bg-blue-50"
                : "border-gray-300 bg-gray-50"
            }`}
          >
            <div className="text-5xl mb-4">
              📤
            </div>

            <p className="text-gray-700 font-medium">
              Drag & Drop your file here
            </p>

            <p className="text-sm text-gray-500 mt-2">
              or
            </p>

            <label className="inline-block mt-4">
              <span className="bg-blue-600 text-white px-5 py-2 rounded-lg cursor-pointer hover:bg-blue-700">
                Choose File
              </span>

              <input
                type="file"
                onChange={handleInputChange}
                className="hidden"
              />
            </label>
          </div>
        )}

        {/* Selected File */}
        {selectedFile && (
          <div>
            <div className="bg-gray-50 rounded-xl p-4 mb-4">
              <div className="flex items-center gap-3">

                <div className="text-3xl">
                  {selectedFile.type.startsWith("image/")
                    ? "🖼️"
                    : selectedFile.type === "application/pdf"
                    ? "📄"
                    : "📁"}
                </div>

                <div className="min-w-0">
                  <p className="font-medium text-gray-800 truncate">
                    {selectedFile.name}
                  </p>

                  <p className="text-sm text-gray-500">
                    {(
                      selectedFile.size /
                      1024 /
                      1024
                    ).toFixed(2)}{" "}
                    MB
                  </p>
                </div>

              </div>
            </div>

            {/* Preview */}
            <div className="mb-4">
              {getFilePreview()}
            </div>

            {/* Progress */}
            {(isUploading || progress > 0) && (
              <div className="mb-4">

                <div className="flex justify-between text-sm mb-2">
                  <span className="text-gray-600">
                    {isUploading
                      ? "Uploading..."
                      : progress === 100
                      ? "Upload complete"
                      : "Upload"}
                  </span>

                  <span className="font-medium text-blue-600">
                    {progress}%
                  </span>
                </div>

                <div className="w-full bg-gray-200 rounded-full h-2">
                  <div
                    className="bg-blue-600 h-2 rounded-full transition-all"
                    style={{
                      width: `${progress}%`,
                    }}
                  />
                </div>

              </div>
            )}

            {/* Message */}
            {message && (
              <div
                className={`mb-4 p-3 rounded-lg text-sm ${
                  isError
                    ? "bg-red-100 text-red-700"
                    : "bg-green-100 text-green-700"
                }`}
              >
                {message}
              </div>
            )}

            {/* Buttons */}
            <div className="flex gap-3">

              <button
                onClick={() => {
                  setSelectedFile(null);
                  setProgress(0);
                  setMessage("");
                  setIsError(false);
                }}
                disabled={isUploading}
                className="flex-1 border border-gray-300 py-3 rounded-lg text-gray-700 hover:bg-gray-100 disabled:opacity-50"
              >
                Choose Another
              </button>

              <button
                onClick={startUpload}
                disabled={isUploading || progress === 100}
                className="flex-1 bg-blue-600 text-white py-3 rounded-lg font-semibold hover:bg-blue-700 disabled:opacity-50"
              >
                {isUploading
                  ? "Uploading..."
                  : progress === 100
                  ? "Uploaded"
                  : "Upload"}
              </button>

            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default UploadFile;