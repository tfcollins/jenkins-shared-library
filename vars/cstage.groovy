def call(name, branch, flags, cls) {

    for (flag in flags) {
        switch(flag[0])
        {
            case 'skip':
                if (flag[1] == name) {
                    stage(name) {
                        echo "Skipping ${name} | Filter: skip"
                    }
                    return
                }
            case 'skip_branch':
                if ((flag[2] == name) && (flag[1] == branch)) {
                    stage(name) {
                        echo "Skipping ${name} | Filter: skip_branch"
                    }
                    return
                }
            case 'enable_only_branch':
                if ((flag[2] == name) && (flag[1] != branch)) {
                    stage(name) {
                        echo "Skipping ${name} | Filter: enable_only_branch"
                    }
                    return
                }
        }
    }
    // Default
    cls(name);
}