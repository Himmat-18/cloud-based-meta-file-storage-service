import { useEffect, useRef, useState } from "react";
import Login from "./Login";
import UploadFile from "./UploadFile";
import { apiFetch } from "./api";

function App() {

  const sharedToken =
    window.location.pathname.startsWith("/shared/")
      ? window.location.pathname.split("/shared/")[1]
      : null;

  const [isLoggedIn, setIsLoggedIn] = useState(
    !!localStorage.getItem("token")
  );

  const [activeMenu, setActiveMenu] = useState("My Files");
  const [currentFolder, setCurrentFolder] = useState("Home");
  const [currentFolderId, setCurrentFolderId] = useState(null);

  const [showUpload, setShowUpload] = useState(false);

  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const [searchText, setSearchText] = useState("");
const [filterType, setFilterType] = useState("all");

const [downloadingId, setDownloadingId] = useState(null);
  const [modifyTarget, setModifyTarget] = useState(null);
  const [modifyLoading, setModifyLoading] = useState(false);
  const [starredIds, setStarredIds] = useState(new Set());
  const [starLoadingId, setStarLoadingId] = useState(null);


  const modifyInputRef = useRef(null);

  const userName = localStorage.getItem("userName") || "User";
  const token = localStorage.getItem("token");
  const userRole = localStorage.getItem("userRole") || "VIEWER";

// =========================
// LOAD STARRED FILES
// =========================

const loadStarredFiles = async () => {
  try {
    setLoading(true);
    setError("");

    const currentToken = localStorage.getItem("token");

    if (!currentToken) {
      return;
    }
const response = await apiFetch("/api/stars", {
      
      
        method: "GET",
        headers: {
          Authorization: `Bearer ${currentToken}`,
        },
      }
    );

    if (!response.ok) {
      const text = await response.text();

      console.log("Starred API status:", response.status);
      console.log("Starred API response:", text);

      throw new Error(
      `Failed to load starred files (${response.status})`
      );
    }

    const stars = await response.json();

    console.log("Starred API data:", stars);

    const starredFiles = stars
  .map((star) => star.file)
  .filter((file) => file)
  .map((file) => ({
    ...file,
    folder: null,
  }));

    setFiles(starredFiles);

    setStarredIds(
      new Set(starredFiles.map((file) => file.id))
    );

  } catch (err) {
    console.error("Load starred files error:", err);
    setError("Failed to load starred files.");
  } finally {
    setLoading(false);
  }
};
// =========================
// LOAD FILES + FOLDERS
// =========================

const loadFiles = async () => {
  try {
    setLoading(true);
    setError("");

    const folderId =
      currentFolderId === null ? 0 : currentFolderId;

    const currentToken = localStorage.getItem("token");

    // =========================
    // LOAD FILES
    // =========================

    const filesResponse = await apiFetch(
      `/api/files?folderId=${folderId}`,
      {
        method: "GET",
        headers: {
          Authorization: `Bearer ${currentToken}`,
          "Content-Type": "application/json",
        },
      }
    );

    if (!filesResponse.ok) {
      const responseText = await filesResponse.text();

      console.log(
        "Files API status:",
        filesResponse.status
      );

      console.log(
        "Files API response:",
        responseText
      );

      throw new Error(
        `Failed to load files (${filesResponse.status})`
      );
    }

    const fileData = await filesResponse.json();

    console.log("Files API data:", fileData);

    // =========================
    // LOAD FOLDERS
    // =========================

    const foldersResponse = await apiFetch(
      "/api/folders",
      {
        method: "GET",
        headers: {
          Authorization: `Bearer ${currentToken}`,
          "Content-Type": "application/json",
        },
      }
    );

    if (!foldersResponse.ok) {
      const responseText = await foldersResponse.text();

      console.log(
        "Folders API status:",
        foldersResponse.status
      );

      console.log(
        "Folders API response:",
        responseText
      );

      throw new Error(
        `Failed to load folders (${foldersResponse.status})`
      );
    }

    const folderData = await foldersResponse.json();

    console.log(
      "Folders API data:",
      folderData
    );

    // =========================
    // CURRENT FOLDER'S FOLDER
    // =========================

    const currentFolders = folderData
      .filter((folder) => {

        // Home ke andar sirf root folders
        if (currentFolderId === null) {
          return folder.parent === null;
        }

        // Current folder ke andar ke folders
        return (
          folder.parent &&
          folder.parent.id === currentFolderId
        );
      })
      .map((folder) => ({
        ...folder,
        folder: true,
        size: null,
      }));

    // =========================
    // COMBINE FOLDERS + FILES
    // =========================

    setFiles([
      ...currentFolders,
      ...fileData,
    ]);

  } catch (err) {
    console.error(
      "Load files/folders error:",
      err
    );

    setError(
      "Failed to load files and folders."
    );

  } finally {
    setLoading(false);
  }
};
// =========================
// SEARCH FILES
// =========================

const handleSearch = async (value) => {
  setSearchText(value);

  // Reload normal files when search box is empty
  if (value.trim() === "") {
    await loadFiles();
    return;
  }

  try {
    setLoading(true);
    setError("");

    const currentToken = localStorage.getItem("token");

    if (!currentToken) {
      alert("Authentication token not found. Please login again.");
      return;
    }

   const response = await apiFetch(
  `/api/files/search?name=${encodeURIComponent(
    value.trim()
  )}`,
    {
        method: "GET",
        headers: {
          Authorization: `Bearer ${currentToken}`,
          "Content-Type": "application/json",
        },
    }
    
    );

    if (!response.ok) {
      const text = await response.text();

      console.log("Search status:", response.status);
      console.log("Search response:", text);

      throw new Error(
        `Search failed (${response.status})`
      );
    }

    const searchResults = await response.json();

    console.log("Search results:", searchResults);

    setFiles(searchResults);

  } catch (err) {
    console.error("Search error:", err);

    setError("Failed to search files.");

  } finally {
    setLoading(false);
  }
};

// =========================
// FILTER FILES
// =========================

const handleFilter = async (value) => {
  setFilterType(value);

  if (value === "all") {
    await loadFiles();
    return;
  }

  try {
    setLoading(true);
    setError("");

    const currentToken = localStorage.getItem("token");

    if (!currentToken) {
      alert("Authentication token not found. Please login again.");
      return;
    }

   const response = await apiFetch(
  `/api/files/filter?contentType=${encodeURIComponent(
    value
  )}`,
  {
    method: "GET",
    headers: {
      Authorization: `Bearer ${currentToken}`,
      "Content-Type": "application/json",
    },
  }
);

    if (!response.ok) {
      const text = await response.text();

      console.log("Filter status:", response.status);
      console.log("Filter response:", text);

      throw new Error(
        `Filter failed (${response.status})`
      );
    }

    const filteredResults = await response.json();

    console.log("Filter results:", filteredResults);

    setFiles(filteredResults);

  } catch (err) {
    console.error("Filter error:", err);

    setError("Failed to filter files.");

  } finally {
    setLoading(false);
  }
};
// =========================
// LOAD TRASH FILES
// =========================

const loadTrashFiles = async () => {
  try {
    setLoading(true);
    setError("");

    const currentToken = localStorage.getItem("token");

    if (!currentToken) {
      return;
    }

    const response = await apiFetch(
      "/api/files/trash",
      {
        method: "GET",
        headers: {
          Authorization: `Bearer ${currentToken}`,
        },
      }
    );

    if (!response.ok) {
      const text = await response.text();

      console.log("Trash API status:", response.status);
      console.log("Trash API response:", text);

      throw new Error(
        `Failed to load trash files (${response.status})`
      );
    }

    const trashFiles = await response.json();

    console.log("Trash API data:", trashFiles);

    const normalizedTrashFiles = trashFiles.map((file) => ({
      ...file,
      folder: null,
    }));

    setFiles(normalizedTrashFiles);

  } catch (err) {
    console.error("Load trash files error:", err);
    setError("Failed to load trash files.");
  } finally {
    setLoading(false);
  }
};
// =========================
// STAR / UNSTAR FILE
// =========================

const handleStarToggle = async (file) => {
  try {
    const currentToken = localStorage.getItem("token");

    if (!currentToken) {
      alert("Authentication token not found. Please login again.");
      return;
    }

    setStarLoadingId(file.id);

    const isCurrentlyStarred =
      starredIds.has(file.id);

    let response;

    if (isCurrentlyStarred) {
      // =========================
      // UNSTAR
      // =========================

      response = await apiFetch(
        `/api/stars/${file.id}`,
        {
          method: "DELETE",
          headers: {
            Authorization: `Bearer ${currentToken}`,
          },
        }
      );

    } else {
      // =========================
      // STAR
      // =========================

      response = await apiFetch(
        `/api/stars?fileId=${file.id}`,
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${currentToken}`,
          },
        }
      );
    }

    if (!response.ok) {
      const text = await response.text();

      console.log(
        "Star API status:",
        response.status
      );

      console.log(
        "Star API response:",
        text
      );

      throw new Error(
        `Star operation failed (${response.status})`
      );
    }

    // =========================
    // UPDATE STARRED IDS
    // =========================

    setStarredIds((previous) => {
      const updated = new Set(previous);

      if (isCurrentlyStarred) {
        updated.delete(file.id);
      } else {
        updated.add(file.id);
      }

      return updated;
    });

    alert(
      isCurrentlyStarred
        ? "File unstarred successfully."
        : "File starred successfully."
    );

  } catch (err) {
    console.error(
      "Star/unstar error:",
      err
    );

    alert(
      "Failed to update star status."
    );

  } finally {
    setStarLoadingId(null);
  }
};
// =========================
// LOAD SHARED FILES
// =========================

const loadSharedFiles = async () => {
  try {
    setLoading(true);
    setError("");

    const currentToken = localStorage.getItem("token");

    if (!currentToken) {
      alert(
        "Authentication token not found. Please login again."
      );
      return;
    }

    const response = await apiFetch(
      "/api/shares/shared-with-me",
      {
        method: "GET",
        headers: {
          Authorization: `Bearer ${currentToken}`,
          "Content-Type": "application/json",
        },
      }
    );

    if (!response.ok) {
      const text = await response.text();

      console.log(
        "Shared files API status:",
        response.status
      );

      console.log(
        "Shared files API response:",
        text
      );

      throw new Error(
        `Failed to load shared files (${response.status})`
      );
    }

    const shares = await response.json();

    console.log(
      "Shared files API data:",
      shares
    );

    const sharedFiles = shares
      .map((share) => {
        if (!share.file) {
          return null;
        }

        return {
          ...share.file,
          folder: null,
          sharedRole: share.role,
        };
      })
      .filter((file) => file !== null);

    setFiles(sharedFiles);

    setStarredIds(
      new Set(
        sharedFiles
          .filter((file) => file.id)
          .map((file) => file.id)
      )
    );

  } catch (err) {
    console.error(
      "Load shared files error:",
      err
    );

    setError(
      "Failed to load shared files."
    );

  } finally {
    setLoading(false);
  }
};
  // =========================
  // LOAD FILES WHEN LOGIN/FOLDER CHANGES
  // =========================

  useEffect(() => {
    if (isLoggedIn) {
      loadFiles();
    }
  }, [isLoggedIn, currentFolderId]);

  // =========================
  // LOGIN
  // =========================

  const handleLogin = () => {
    setIsLoggedIn(true);
  };

  // =========================
  // LOGOUT
  // =========================

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("userId");
    localStorage.removeItem("userName");
    localStorage.removeItem("userEmail");
    localStorage.removeItem("userRole");

    setIsLoggedIn(false);
    setFiles([]);
  };

  // =========================
  // DOWNLOAD FILE
  // =========================

  const handleDownload = async (file) => {
    try {
      const userId = localStorage.getItem("userId");
      const currentToken = localStorage.getItem("token");

      if (!userId) {
        alert("User ID not found. Please login again.");
        return;
      }

      if (!currentToken) {
        alert("Authentication token not found. Please login again.");
        return;
      }

      setDownloadingId(file.id);

      const response = await apiFetch(
       `/api/files/${file.id}/download`,
        {
          method: "GET",
          headers: {
            Authorization: `Bearer ${currentToken}`,
          },
        }
      );

      if (!response.ok) {
        const errorText = await response.text();

        console.log("Download status:", response.status);
        console.log("Download response:", errorText);

        throw new Error(
          `Download failed (${response.status})`
        );
      }

      const blob = await response.blob();

      const downloadUrl =
        window.URL.createObjectURL(blob);

      const link = document.createElement("a");

      link.href = downloadUrl;
      link.download = file.name;

      document.body.appendChild(link);

      link.click();

      link.remove();

      window.URL.revokeObjectURL(downloadUrl);
    } catch (err) {
      console.error("Download error:", err);
      alert("Failed to download file.");
    } finally {
      setDownloadingId(null);
    }
  };

  // =========================
  // MODIFY FILE - OPEN FILE PICKER
  // =========================

  const handleModifyClick = (file) => {
    setModifyTarget(file);

    if (modifyInputRef.current) {
      modifyInputRef.current.click();
    }
  };

  // =========================
  // MODIFY FILE - UPLOAD NEW VERSION
  // =========================

  const handleModifySelected = async (event) => {
    const selectedFile = event.target.files?.[0];

    if (!selectedFile || !modifyTarget) {
      return;
    }

    try {
      const currentToken = localStorage.getItem("token");

      if (!currentToken) {
        alert("Authentication token not found. Please login again.");
        return;
      }

      setModifyLoading(true);

      const formData = new FormData();

      formData.append("file", selectedFile);

      const response = await apiFetch(
        `/api/files/${modifyTarget.id}/modify`,
        {
          method: "PUT",
          headers: {
            Authorization: `Bearer ${currentToken}`,
          },
          body: formData,
        }
      );

      if (!response.ok) {
        const text = await response.text();

        console.log("Modify status:", response.status);
        console.log("Modify response:", text);

        throw new Error(
          `Modify failed (${response.status})`
        );
      }

      await response.json();

      alert("File modified successfully.");

      await loadFiles();
    } catch (err) {
      console.error("Modify error:", err);
      alert("Failed to modify file.");
    } finally {
      setModifyLoading(false);
      setModifyTarget(null);

      event.target.value = "";
    }
  };

  // =========================
  // RENAME FILE
  // =========================

  const handleRename = async (file) => {
    const newName = window.prompt(
      "Enter new file name:",
      file.name
    );

    if (!newName || newName.trim() === "") {
      return;
    }

    if (newName.trim() === file.name) {
      return;
    }

    try {
      const currentToken = localStorage.getItem("token");

      if (!currentToken) {
        alert("Authentication token not found. Please login again.");
        return;
      }

      const response = await apiFetch(
        `/api/files/${file.id}/rename?newName=${encodeURIComponent(
          newName.trim()
        )}`,
        {
          method: "PUT",
          headers: {
            Authorization: `Bearer ${currentToken}`,
          },
        }
      );

      if (!response.ok) {
        const text = await response.text();

        console.log("Rename status:", response.status);
        console.log("Rename response:", text);

        throw new Error(
          `Rename failed (${response.status})`
        );
      }

      await response.json();

      alert("File renamed successfully.");

      await loadFiles();
    } catch (err) {
      console.error("Rename error:", err);
      alert("Failed to rename file.");
    }
  };

  // =========================
  // MOVE FILE
  // =========================

  const handleMove = async (file) => {
    const targetFolderId = window.prompt(
      "Enter target folder ID:"
    );

    if (!targetFolderId) {
      return;
    }

    try {
      const currentToken = localStorage.getItem("token");

      if (!currentToken) {
        alert("Authentication token not found. Please login again.");
        return;
      }

      const response = await apiFetch(
        `/api/files/${file.id}/move?targetFolderId=${targetFolderId}`,
      
        
        {
          method: "PUT",
          headers: {
            Authorization: `Bearer ${currentToken}`,
          },
        }
      );

      if (!response.ok) {
        const text = await response.text();

        console.log("Move status:", response.status);
        console.log("Move response:", text);

        throw new Error(
          `Move failed (${response.status})`
        );
      }

      await response.json();

      alert("File moved successfully.");

      await loadFiles();
    } catch (err) {
      console.error("Move error:", err);
      alert("Failed to move file.");
    }
  };

  // =========================
  // DELETE FILE
  // =========================

  const handleDelete = async (file) => {
    const confirmed = window.confirm(
      `Move "${file.name}" to Trash?`
    );

    if (!confirmed) {
      return;
    }

    try {
      const userId = localStorage.getItem("userId");
      const currentToken = localStorage.getItem("token");

      if (!userId) {
        alert("User ID not found. Please login again.");
        return;
      }

      if (!currentToken) {
        alert("Authentication token not found. Please login again.");
        return;
      }

      const response = await apiFetch(
        `/api/files/${file.id}`,
        {
          method: "DELETE",
          headers: {
            Authorization: `Bearer ${currentToken}`,
          },
        }
      );

      if (!response.ok) {
        const text = await response.text();

        console.log("Delete status:", response.status);
        console.log("Delete response:", text);

        throw new Error(
          `Delete failed (${response.status})`
        );
      }

      alert("File moved to Trash.");

      await loadFiles();
    } catch (err) {
      console.error("Delete error:", err);
      alert("Failed to delete file.");
    }
  };

  // =========================
// CREATE PUBLIC SHARE LINK
// =========================

 const handleCreatePublicLink = async (file) => {
  try {
    const currentToken = localStorage.getItem("token");

    if (!currentToken) {
      alert("Authentication token not found. Please login again.");
      return;
    }

    const expiryInput = window.prompt(
      "Enter link expiry time in minutes:\n\n0 = Never expires"
    );

    if (expiryInput === null) {
      return;
    }

    const expiryMinutes = Number(expiryInput);

    if (
      isNaN(expiryMinutes) ||
      expiryMinutes < 0 ||
      !Number.isInteger(expiryMinutes)
    ) {
      alert("Please enter a valid number.");
      return;
    }

    const password = window.prompt(
      "Enter password for this link:\n\nLeave empty for no password."
    );

    if (password === null) {
      return;
    }

    let url =
      `/api/link-shares?fileId=${file.id}`;

    if (expiryMinutes > 0) {
      url += `&expiryMinutes=${expiryMinutes}`;
    }

    if (password.trim() !== "") {
      url += `&password=${encodeURIComponent(password)}`;
    }

    const response = await apiFetch(url, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${currentToken}`,
      },
    });

    if (!response.ok) {
      const text = await response.text();

      console.log("Public share status:", response.status);
      console.log("Public share response:", text);

      throw new Error(
        `Public share failed (${response.status})`
      );
    }

    const linkShare = await response.json();

    const publicLink =
      `${window.location.origin}/shared/${linkShare.token}`;

    await navigator.clipboard.writeText(publicLink);

    alert(
      "Public share link created and copied to clipboard."
    );

  } catch (err) {
    console.error(
      "Create public link error:",
      err
    );

    alert(
      "Failed to create public share link."
    );
  }
};
// =========================
// RESTORE FILE
// =========================

const handleRestore = async (file) => {
  try {
    const currentToken = localStorage.getItem("token");

    const response = await apiFetch(
      `/api/files/${file.id}/restore`,
      {
        method: "PUT",
        headers: {
          Authorization: `Bearer ${currentToken}`,
        },
      }
    );

    if (!response.ok) {
      const text = await response.text();

      console.log("Restore API status:", response.status);
      console.log("Restore API response:", text);

      throw new Error(
        `Restore failed (${response.status})`
      );
    }

    alert("File restored successfully");

    loadTrashFiles();

  } catch (err) {
    console.error("Restore error:", err);
    alert("Failed to restore file");
  }
};
//==========================
// CREATE FOLDER
// =========================

const handleCreateFolder = async () => {
  const folderName = window.prompt("Enter folder name:");

  if (!folderName || folderName.trim() === "") {
    return;
  }

  try {
    const currentToken = localStorage.getItem("token");

    if (!currentToken) {
      alert("Authentication token not found. Please login again.");
      return;
    }

    let url =
      `/api/folders?name=${encodeURIComponent(
        folderName.trim()
      )}`;

    if (currentFolderId !== null) {
      url += `&parentId=${currentFolderId}`;
    }

    const response = await apiFetch(url, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${currentToken}`,
      },
    });

    if (!response.ok) {
      const text = await response.text();

      console.log("Create folder status:", response.status);
      console.log("Create folder response:", text);

      throw new Error(
        `Create folder failed (${response.status})`
      );
    }

    await response.json();

    alert("Folder created successfully.");

    await loadFiles();

  } catch (err) {
    console.error("Create folder error:", err);
    alert("Failed to create folder.");
  }
};

  // =========================
  // FILE TYPE
  // =========================

  const getFileType = (file) => {
    if (!file.contentType) {
      return "File";
    }

    if (file.contentType.includes("pdf")) {
      return "PDF";
    }

    if (file.contentType.includes("image")) {
      return "Image";
    }

    if (file.contentType.includes("text")) {
      return "Text";
    }

    if (
      file.contentType.includes("word") ||
      file.contentType.includes("document")
    ) {
      return "Document";
    }

    if (
      file.contentType.includes("java") ||
      file.name?.endsWith(".java")
    ) {
      return "Java";
    }

    return "File";
  };

  // =========================
  // FORMAT FILE SIZE
  // =========================

  const formatSize = (bytes) => {
    if (!bytes) {
      return "0 B";
    }

    if (bytes < 1024) {
      return `${bytes} B`;
    }

    if (bytes < 1024 * 1024) {
      return `${(bytes / 1024).toFixed(1)} KB`;
    }

    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  };

  // =========================
  // FOLDER CLICK
  // =========================

  const handleFolderClick = (file) => {
    if (file.folder) {
      setCurrentFolder(file.name);
      setCurrentFolderId(file.id);
    }
  };

  // =========================
  // SIDE MENU
  // =========================

    const handleMenuClick =
    (menu) => {
   setActiveMenu(menu);

  if (menu === "My Files") {
    setCurrentFolder("Home");
    setCurrentFolderId(null);
    setSearchText("");
    
  }

  if (menu === "Starred") {
    setCurrentFolder("Starred");
    setCurrentFolderId(null);
    setSearchText("");
    loadStarredFiles();
  }
  if (menu === "Shared") {
    setCurrentFolder("Shared");
    setCurrentFolderId(null);
    setSearchText("");
    loadSharedFiles();
  }
   if (menu === "Trash") {
   setCurrentFolder("Trash");
   setCurrentFolderId(null);
   setSearchText("");
   loadTrashFiles();
  }
};

  // =========================
  // SEARCH
  // =========================

 const filteredFiles = files.filter((file) => {
  const name = file.name || "";

  return name
    .toLowerCase()
    .includes(searchText.toLowerCase().trim());
});
  // =========================
// PUBLIC SHARED FILE
// =========================

if (sharedToken) {
  const handlePublicDownload = () => {
    const password = window.prompt(
      "Enter password:"
    );

    if (password === null) {
      return;
    }

    const downloadUrl =
      `http://localhost:8080/api/link-shares/${sharedToken}/download?password=${encodeURIComponent(password)}`;

    window.location.href = downloadUrl;
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">

      <div className="bg-white p-8 rounded-xl shadow-md text-center">

        <h1 className="text-2xl font-bold mb-4">
          Shared File
        </h1>

        <p className="text-gray-600 mb-6">
          You have received a shared file.
        </p>

        <button
          onClick={handlePublicDownload}
          className="bg-blue-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-blue-700"
        >
          Download File
        </button>

      </div>

    </div>
  );
}

  // =========================
  // LOGIN PAGE
  // =========================

  if (!isLoggedIn) {
    return <Login onLogin={handleLogin} />;
  }

  // =========================
  // DASHBOARD
  // =========================

  return (
    <div className="min-h-screen bg-gray-100 flex">

      {/* =========================
          SIDEBAR
          ========================= */}

      <aside className="w-64 bg-white border-r border-gray-200 p-5 hidden md:block">

        <h1 className="text-2xl font-bold text-blue-600 mb-8">
          Cloud Storage
        </h1>

        <nav className="space-y-2">

          {[
            "My Files",
            "Starred",
            "Shared",
            "Trash",
          ].map((menu) => (

            <button
              key={menu}
              onClick={() => handleMenuClick(menu)}
              className={`w-full text-left px-4 py-3 rounded-lg transition ${
                activeMenu === menu
                  ? "bg-blue-100 text-blue-600 font-semibold"
                  : "text-gray-600 hover:bg-gray-100"
              }`}
            >
              {menu}
            </button>

          ))}

        </nav>

        <button
          onClick={() => setShowUpload(true)}
          className="w-full mt-8 bg-blue-600 text-white py-3 rounded-lg font-semibold hover:bg-blue-700"
        >
          + Upload File
        </button>

      </aside>

      {/* =========================
          MAIN CONTENT
          ========================= */}

      <main className="flex-1">

        {/* HEADER */}

        <header className="bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between">

          <div>

            <h2 className="text-xl font-semibold text-gray-800">
              {activeMenu}
            </h2>

            <p className="text-sm text-gray-500">
              Manage your files and folders
            </p>

          </div>

          {/* USER */}

          <div className="flex items-center gap-3">

            <div className="text-right hidden sm:block">

              <p className="text-sm font-medium text-gray-800">
                {userName}
              </p>

              <p className="text-xs text-gray-500">
                Role: {userRole}
              </p>

              <button
                onClick={handleLogout}
                className="text-xs text-red-600 hover:underline"
              >
                Logout
              </button>

            </div>

            <div className="w-10 h-10 bg-blue-100 text-blue-600 rounded-full flex items-center justify-center font-bold">
              {userName.charAt(0).toUpperCase()}
            </div>

          </div>

        </header>

        {/* =========================
            CONTENT SECTION
            ========================= */}

        <section className="p-6">

          {/* BREADCRUMB */}

          <div className="flex items-center gap-2 text-sm text-gray-500 mb-6">

            <button
              onClick={() => {
                setCurrentFolder("Home");
                setCurrentFolderId(null);
                setActiveMenu("My Files");
              }}
              className="text-blue-600 font-medium hover:underline"
            >
              My Files
            </button>

            <span>/</span>

            <span>
              {currentFolder}
            </span>

          </div>

         {/* SEARCH + UPLOAD */}

<div className="flex flex-col md:flex-row gap-4 justify-between mb-6">

  <input
  type="text"
  placeholder="Search files..."
  value={searchText}
  onChange={(e) =>
    handleSearch(e.target.value)
  }
  className="w-full md:w-96 px-4 py-3 bg-white border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
/>
<select
  value={filterType}
  onChange={(e) =>
    handleFilter(e.target.value)
  }
  className="px-4 py-3 bg-white border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
>
  <option value="all">All Files</option>
  <option value="pdf">PDF</option>
  <option value="image">Images</option>
  <option value="text">Text</option>
  <option value="word">Documents</option>
  <option value="java">Java</option>
</select>
    
  <div className="flex gap-3">

    <button
      onClick={handleCreateFolder}
      className="bg-green-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-green-700"
    >
      + New Folder
    </button>

    <button
      onClick={() => setShowUpload(true)}
      className="bg-blue-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-blue-700"
    >
      + Upload File
    </button>

  </div>

</div>


          {/* ERROR */}

          {error && (
            <div className="mb-4 p-4 bg-red-100 text-red-700 rounded-lg">
              {error}
            </div>
          )}

          {/* =========================
              FILE TABLE
              ========================= */}

          <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">

            {/* TABLE HEADER */}

            <div className="grid grid-cols-5 gap-4 px-6 py-4 bg-gray-50 border-b text-sm font-semibold text-gray-600">

              <div>
                Name
              </div>

              <div>
                Type
              </div>

              <div>
                Size
              </div>

              <div>
                Modified
              </div>

              <div>
                Actions
              </div>

            </div>

            {/* LOADING */}

            {loading && (
              <div className="p-8 text-center text-gray-500">
                Loading files...
              </div>
            )}

            {/* NO FILES */}

            {!loading &&
              filteredFiles.length === 0 && (
                <div className="p-8 text-center text-gray-500">
                  No files found.
                </div>
              )}

            {/* FILE LIST */}

            {!loading &&
              filteredFiles.map((file) => {

                const type = getFileType(file);

                return (

                  <div
                    key={`$file.folder ? "folder" : "file"}-${file.id}`}
                    className="grid grid-cols-5 gap-4 px-6 py-4 border-b last:border-b-0 hover:bg-gray-50 items-center"
                  >

                    {/* NAME */}

                    <div className="flex items-center gap-3">

                      <span className="text-xl">
                        {file.folder ? "📁" : "📄"}
                      </span>

                      <button
                        onClick={() =>
                          handleFolderClick(file)
                        }
                        className={`font-medium ${
                          file.folder
                            ? "text-blue-600 hover:underline cursor-pointer"
                            : "text-gray-800"
                        }`}
                      >
                        {file.name}
                      </button>

                    </div>

                    {/* TYPE */}

                    <div className="text-gray-600">
                      {type}
                    </div>

                    {/* SIZE */}

                    <div className="text-gray-600">
                      {file.folder
                        ? "--"
                        : formatSize(file.size)}
                    </div>

                    {/* MODIFIED */}

                    <div className="text-gray-600">
                      --
                    </div>

                    {/* ACTIONS */}
                  

<div className="flex flex-wrap gap-2">

  {/* TRASH - RESTORE */}

  {activeMenu === "Trash" && !file.folder && (
    <button
      onClick={() => handleRestore(file)}
      className="px-3 py-1 bg-green-600 text-white rounded hover:bg-green-700 text-sm"
    >
      Restore
    </button>
  )}

  {/* NORMAL FILE ACTIONS */}

  {activeMenu !== "Trash" && file.folder !== true &&(
    <>
      {/* STAR */}

      <button
        onClick={() => handleStarToggle(file)}
        disabled={starLoadingId === file.id}
        className="px-3 py-1 bg-yellow-100 text-yellow-700 rounded hover:bg-yellow-200 text-sm disabled:opacity-50"
      >
        {starLoadingId === file.id
          ? "..."
          : starredIds.has(file.id)
          ? "★ Unstar"
          : "☆ Star"}
      </button>

      {/* DOWNLOAD */}

      <button
        onClick={() => handleDownload(file)}
        disabled={downloadingId === file.id}
        className="px-3 py-1 bg-blue-600 text-white rounded hover:bg-blue-700 text-sm disabled:opacity-50"
      >
        {downloadingId === file.id
          ? "Downloading..."
          : "Download"}
      </button>

      {/* EDITOR / OWNER ACTIONS */}

      {(file.sharedRole === "EDITOR" ||
        (!file.sharedRole &&
          (userRole === "EDITOR" ||
            userRole === "OWNER"))) && (
        <>
          <button
            onClick={() => handleModifyClick(file)}
            disabled={modifyLoading}
            className="px-3 py-1 bg-yellow-500 text-white rounded hover:bg-yellow-600 text-sm disabled:opacity-50"
          >
            {modifyLoading &&
            modifyTarget?.id === file.id
              ? "Modifying..."
              : "Modify"}
          </button>

          <button
            onClick={() => handleRename(file)}
            className="px-3 py-1 bg-green-600 text-white rounded hover:bg-green-700 text-sm"
          >
            Rename
          </button>

          <button
            onClick={() => handleMove(file)}
            className="px-3 py-1 bg-purple-600 text-white rounded hover:bg-purple-700 text-sm"
          >
            Move
          </button>
        </>
      )}

      {/* PUBLIC SHARE */}

<button
  onClick={() => handleCreatePublicLink(file)}
  className="px-3 py-1 bg-indigo-600 text-white rounded hover:bg-indigo-700 text-sm"
>
  🔗 Share
</button>

      {/* OWNER ONLY - DELETE */}

      {!file.sharedRole &&
        userRole === "OWNER" && (
          <button
            onClick={() => handleDelete(file)}
            className="px-3 py-1 bg-red-600 text-white rounded hover:bg-red-700 text-sm"
          >
            Delete
          </button>
        )}
    </>
 )}

</div>
</div>
              );
            })}

          </div>

        </section>

      </main>

      {/* =========================
          HIDDEN MODIFY FILE INPUT
          ========================= */}

      <input
        ref={modifyInputRef}
        type="file"
        className="hidden"
        onChange={handleModifySelected}
      />

      {/* =========================
          UPLOAD MODAL
          ========================= */}

      {showUpload && (
        <UploadFile
        folderId={currentFolderId}
          onClose={() => {
            setShowUpload(false);
            loadFiles();
          }}
        />
      )}

    </div>
  );
}

export default App;